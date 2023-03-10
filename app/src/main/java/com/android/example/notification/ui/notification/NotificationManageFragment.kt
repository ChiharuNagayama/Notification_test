package com.android.example.notification.ui.notification

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.getIntent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.example.notification.MainActivity
import com.android.example.notification.MainApplication
import com.android.example.notification.R
import com.android.example.notification.constant.MyConstant.Companion.CHANNEL_ID
import com.android.example.notification.constant.MyConstant.Companion.CHANNEL_OTHER_ID
import com.android.example.notification.constant.MyConstant.Companion.CHANNEL_X_ID
import com.android.example.notification.databinding.FragmentNotificationManageBinding
import com.android.example.notification.room.MyDataBase
import com.android.example.notification.room.NotificationDataBase
import com.android.example.notification.room.dao.NotificationDao
import com.android.example.notification.room.data.NotificationTableData
import com.android.example.notification.ui.base.list.BaseRecycleViewAdapter
import com.android.example.notification.utils.ColorChangeDialog

import com.android.example.notification.utils.CustomDialog
import com.android.example.notification.utils.FilterDialog
import com.android.example.notification.utils.LoadingDialogUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
/**
 * A simple [Fragment] subclass.
 * Use the [NotificationManageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationManageFragment : Fragment() {
    private  var notificationsViewModel: NotificationManageViewModel? = null
    private var mLoadingDialog: Dialog? = null
    private var _binding: FragmentNotificationManageBinding? = null
    private val binding get() = _binding!!
    private lateinit var frequencylist: Array<String>
    private lateinit var frequencylistSub: Array<String>
    private var notificationsListData = mutableListOf<NotificationTableData>()
    var isNotificationChannelEnable: Boolean = false
    private var dataBase: NotificationDataBase? = null
    var notificationDao: NotificationDao? = null

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // ???????????????????????????????????????????????????
        } else {
            // ??????????????????????????????????????????????????????(1??????)
            // ??????????????????????????????????????????????????????????????????
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Snackbar
                    .make(binding.root, "????????????????????????????????????????????????", Snackbar.LENGTH_LONG)
                    .show()
            } else {
                // ??????????????????????????????????????????????????????(2??????)
                // ??????????????????????????????????????????????????????
                Snackbar
                    .make(binding.root, "????????????????????????????????????????????????????????????????????????", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationManageBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //????????????????????????????????????
        dataBase = MainApplication.instance().notificationDataBase
        initData()
        initView()
        //????????????Channel?????????
        notificationChannelCreate()
        //???????????????Channel?????????
        notificationOtherChannelCreate()
        //??????xxxx????????????Channel?????????
        notificationXChannelCreate()
        // ??????????????????On?????????
        val notificationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
        if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        return root
    }
    override fun onResume() {
        super.onResume()
        //????????????Swich???????????????
        isNotificationChannelEnable = checkNotificationsChannelEnabled(requireContext(),CHANNEL_ID)
        binding.paySwitch.isChecked = isNotificationChannelEnable
        //???????????????Swich???????????????
        isNotificationChannelEnable = checkNotificationsChannelEnabled(requireContext(),CHANNEL_OTHER_ID)
        binding.otherSwitch.isChecked = isNotificationChannelEnable
        //xxxx??????Swich???????????????
        isNotificationChannelEnable = checkNotificationsChannelEnabled(requireContext(),CHANNEL_X_ID)
        binding.xSwitch.isChecked = isNotificationChannelEnable
        //????????????????????????
        notificationDataSet()
    }
    private fun initData(){
        //Room??????????????????Dao?????????
        notificationDao = dataBase?.notificationDao()
        notificationsViewModel = dataBase?.let { NotificationManageViewModel(it) }
        //DB?????????????????????????????????
        activity.let {notificationsViewModel?.getNotificationDataFromDB()}
        //??????????????????????????????????????????????????????????????????????????????????????????
        frequencylist = resources.getStringArray(R.array.frequency)
        //????????????????????????????????????????????????????????????
        frequencylistSub= resources.getStringArray(R.array.week)
    }
    private fun initView() {
        //??????????????????
        val title = binding.titleSetting
        title.title.text = getString(R.string.notification_btn)
        //PullDownRefresh???DiaLog?????????
        var loadingDialog = LoadingDialogUtils()

        //????????????Textview
        val payText = binding.payTxt
        //?????????????????????????????????????????????????????????
        payText.setOnClickListener{
            context?.let { it1 -> dialogShow(it1) }
        }
        //???????????????????????????
        val filterImage = binding.filterIv
        //????????????????????????????????????????????????
        filterImage.setOnClickListener{
            context?.let { it1 -> filterDialogShow(it1,isNotificationChannelEnable) }
        }
        //????????????????????????????????????
        notificationsViewModel?.loadingLiveData?.observe(viewLifecycleOwner) {
            if (it) {
                mLoadingDialog = loadingDialog.createLoadingDialog(activity, "Loading")
            } else {
                loadingDialog.closeDialog(mLoadingDialog)
            }
        }
        //????????????????????????????????????????????????????????????
        val swipeRefreshLayout: SwipeRefreshLayout = binding.refresh
        swipeRefreshLayout.setOnRefreshListener {
            notificationsViewModel?.getPTRNotificationsList()
        }
        //??????????????????????????????????????????
        notificationsViewModel?.pullToRefreshLiveData?.observe(viewLifecycleOwner) {
            swipeRefreshLayout.isRefreshing = it
        }
    }

    /**
     * ????????????Channel?????????
     */
    private fun notificationChannelCreate(){
        // Create the NotificationChannel
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        val isNotificationChannelEnable = checkNotificationsChannelEnabled(requireContext(),CHANNEL_ID)
        //??????????????????
        binding.paySwitch.isChecked = isNotificationChannelEnable
        binding.paySwitch.setOnClickListener {
            // ??????????????????
            val notificationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                //App?????????????????????????????????????????????????????????????????????
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, "com.android.example.notification")
                }
                startActivity(intent)
            }
            else {
                //???????????????????????????????????????
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, "com.android.example.notification")
                    putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
                }
                startActivity(intent)
            }
        }
    }
    /**
     * ???????????????Channel?????????
     */
    private fun notificationOtherChannelCreate(){
        // ???????????????????????????
        val name = getString(R.string.other_notify)
        val descriptionText = getString(R.string.channel_other_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_OTHER_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        //????????????????????????/??????????????????
        val isNotificationChannelEnable = checkNotificationsChannelEnabled(requireContext(),CHANNEL_OTHER_ID)
        //??????????????????
        binding.otherSwitch.isChecked = isNotificationChannelEnable
        //???????????????????????????????????????
        binding.otherSwitch.setOnClickListener {
            // ??????????????????
            val notificationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                //App?????????????????????????????????????????????????????????????????????
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, "com.android.example.notification")
                }
                startActivity(intent)
            }
            else {
                //???????????????????????????????????????
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, "com.android.example.notification")
                    putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_OTHER_ID)
                }
                startActivity(intent)
            }
        }
    }
    /**
     * xxxx??????Channel?????????
     */
    private fun notificationXChannelCreate(){
        // Create the NotificationOtherChannel
        val name = "XXX??????"
        val descriptionText = getString(R.string.channel_other_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_X_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        val isNotificationChannelEnable = checkNotificationsChannelEnabled(requireContext(),CHANNEL_X_ID)
        //??????????????????
        binding.xSwitch.isChecked = isNotificationChannelEnable
        binding.xSwitch.setOnClickListener {
            // ??????????????????
            val notificationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, "com.android.example.notification")
                }
                startActivity(intent)
            }
            else {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, "com.android.example.notification")
                    putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_X_ID)
                }
                startActivity(intent)
            }
        }
    }

    /**
     * fireBase??????????????????????????????
     */
    private fun notificationDataSet(){
            //Room??????????????????????????????????????????
            notificationsListData = notificationDao?.getAll() as MutableList<NotificationTableData>
            if(notificationsListData.isNotEmpty()){
                //????????????????????????????????????
                binding.notificationList.visibility = View.VISIBLE
                binding.errorMsg.visibility = View.GONE
                //??????????????????
                notificationListShow()

        } else{
            //???????????????????????????????????????
            binding.notificationList.visibility = View.GONE
            binding.errorMsg.visibility = View.VISIBLE
        }
    }

    /**
     * ??????????????????
     */
    private fun notificationListShow(){
        //?????????????????????????????????????????????????????????
        var init: (View, NotificationTableData) -> Unit = { v: View, d: NotificationTableData ->
            var cardView = v.findViewById<MaterialCardView>(R.id.card_view)
            var dateView = v.findViewById<TextView>(R.id.date)
            var shopNameView = v.findViewById<TextView>(R.id.shop_name_txt)
            var categoryView = v.findViewById<TextView>(R.id.category_tx)
            var moneyView = v.findViewById<TextView>(R.id.money_tx)
            dateView.text = d.dateTime
            shopNameView.text = d.shopName
            categoryView.text = d.category
            moneyView.text = d.money+"???"
            //??????????????????????????????????????????????????????????????????
            //????????????
            //????????????
            //???????????????
            when(d.category){
                "??????" -> cardView.strokeColor = context?.getColor(R.color.blue)!!
                "??????" -> cardView.strokeColor = context?.getColor(R.color.red)!!
                "?????????" -> cardView.strokeColor = context?.getColor(R.color.gray)!!
                else -> cardView.strokeColor = context?.getColor(R.color.gray)!!
            }

        }
        //?????????????????????????????????
        var adapter =
            NotificationListViewAdapter(
                R.layout.notification_item,
                notificationsListData as ArrayList<NotificationTableData>, init
            )
        binding.notificationList.layoutManager = LinearLayoutManager(activity)
        binding.notificationList.adapter = adapter
        //????????????????????????????????????????????????
        adapter.setRecyclerItemClickListener(object :
            BaseRecycleViewAdapter.OnRecyclerItemClickListener {
            override fun onRecyclerItemClick(view: View, Position: Int) {
                //????????????????????????Item????????????????????????????????????????????????????????????
                val bundle = bundleOf(
                    "money" to adapter.items[Position].money,
                    "date" to adapter.items[Position].dateTime,
                    "shopName" to adapter.items[Position].shopName,
                    "category" to adapter.items[Position].category
                )
                //???????????????????????????
                findNavController().navigate(R.id.division_action,bundle)
            }
        })
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     * @return true ?????????
     */
    private fun checkNotificationsEnabled(): Boolean {
        val notificationManagerCompat =
            NotificationManagerCompat.from(requireContext())
        return notificationManagerCompat.areNotificationsEnabled()
    }
    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param context
     * @param channelID ???????????? id
     * @return true ?????????
     */
    private fun checkNotificationsChannelEnabled(context: Context, channelID: String?): Boolean {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelID)
        return checkNotificationsEnabled() &&
                channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param context Context
     */
    private fun dialogShow(context:Context){
        var customDialog = CustomDialog()
        //SharedPreferences???Key"sp_name???????????????????????????
        val sp: SharedPreferences = context.getSharedPreferences("sp_name", Context.MODE_PRIVATE)
        //????????????????????????
        val freqIndex = sp.getInt("freqIndex", 0)
        //??????????????????????????????????????????????????????????????????
        //??????????????????????????????
        val freqIndexSub = sp.getInt("freqIndexSub", 0)
        val frequencyDialog = customDialog.createDialog(freqIndex,freqIndexSub,context,R.style.CustomDialog)
        //????????????????????????????????????????????????????????????
        customDialog.settingInfo(context,frequencyDialog)
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     * @param context Context
     * @param isPayCheck Boolean
     */
    private fun filterDialogShow(context:Context,isPayCheck:Boolean){
        var filterDialog = FilterDialog()
        var indexList= arrayListOf <Map<String, Int>>()
        var index = mutableMapOf <String, Int>()
        val sp: SharedPreferences = context.getSharedPreferences("sp_filter", Context.MODE_PRIVATE)
        //?????????????????????????????????????????????????????????????????????SharedPreferences?????????
        val startIndex = sp.getInt("dateStart", 0)
        index["dateStart"] = startIndex
        indexList.add(index)
        val endIndex = sp.getInt("dateEnd", 0)
        index["dateEnd"] = endIndex
        indexList.add(index)
        val typesIndex = sp.getInt("type", 0)
        index["type"] = typesIndex
        indexList.add(index)
        val categoryIndex = sp.getInt("category", 0)
        index["category"] = categoryIndex
        indexList.add(index)
        val frequencyDialog = filterDialog.createDialog(indexList,context,R.style.CustomDialog,isPayCheck)
        filterDialog.settingInfo(context,frequencyDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}