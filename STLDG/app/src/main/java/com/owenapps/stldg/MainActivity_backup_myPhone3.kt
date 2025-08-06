package com.owenapps.stldg

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.owenapps.stldg.model.UserData
import com.owenapps.stldg.view.UserAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity_backup_myPhone3 : AppCompatActivity() {

    private lateinit var addBetsBtn:Button
    private lateinit var recv:RecyclerView
    private lateinit var betList:ArrayList<UserData>
    private lateinit var userAdapter:UserAdapter
    private lateinit var sendBetsButton: Button
    private lateinit var infoButton: ImageButton
    private var VERSION = "v2.4.0"
    private var HIGH_BETS_SMART = "+639638749595"
    private var HIGH_BETS_GLOBE = "+639368306293"
    private var SMALL_BETS_SMART1 = "+639195698921"
    private var SMALL_BETS_SMART2 = "+639424872464"
    private var SMALL_BETS_GLOBE1 = "+639759111579"
    private var SMALL_BETS_GLOBE2 = "+639974152020"
    private val DRAW_TIME_2PM = "2PM"
    private val DRAW_TIME_5PM = "5PM"
    private val DRAW_TIME_9PM = "9PM"
    private val DAY_OF_MONTH = "dayOfMonth"
    private val NETWORK_SMART = 1
    private val NETWORK_GLOBE = 2
    private var currentNetwork = NETWORK_GLOBE
    private var DGLOG_PREFIX = "DGLOG_"
    private var TARP_CODE = "tarpCode"
    private val array_HOT_NUMBERS = arrayOf("123","213","231","312","132","321","120","201","102","502","250","104","125","129",
        "138","143","163","513", "122","221","212","242","224","422","112","212","131","113",
        "424","244","101","110","525","220","100","500","117","214","202","103","119")
    val PERMISSION_BLUETOOTH = 1
    private val REQUEST_CODE = 100

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )
    private val PERMISSIONS_LOCATION = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val storage =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (storage.getInt("dayOfMonth", 0) == 0) {  //initial checking. set to 0 if not yet set
            var editor = storage.edit()
            editor.putInt("dayOfMonth", currentDay)
            editor.apply()
        }
/*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            Toast.makeText(this,"Permission to Send SMS not granted.",Toast.LENGTH_LONG).show()

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,"Manifest.permission.SEND_SMS")) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this,"Permission to Send SMS denied.",Toast.LENGTH_LONG).show()
            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, arrayOf("Manifest.permission.SEND_SMS"), REQUEST_CODE);
                // REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                Toast.makeText(this,"Requesting permission...",Toast.LENGTH_LONG).show()
            }
        }
        else {
            // Permission has already been granted
        }
*/
        /**set List*/
        betList = ArrayList()
        /**set find Id*/
        addBetsBtn = findViewById(R.id.addBetsButton)
        recv = findViewById(R.id.mRecycler)
        /**set Adapter*/
        userAdapter = UserAdapter(this,betList)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = userAdapter

        infoButton = findViewById(R.id.infoButton)
        /**set Dialog*/
        addBetsBtn.setOnClickListener {
            if (storage.getString(TARP_CODE, "") == "") {
                inputTarpCode(storage)
            } else {
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if (hour in 6..20) {
                    if (betList.size < 10) {
                        addBets(storage)
                    } else {
                        Toast.makeText(this,"Limit to 10 bets only.",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this,"Betting is now closed. Betting is open between 6:00am-8:50pm",Toast.LENGTH_LONG).show()
                }

            }
        }

        infoButton.setOnClickListener {
            showInfo(calendar, storage)
            /*
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                        R.id.salesInfo -> {
                            showInfo(calendar, storage)
                            true
                        }
                        R.id.salesLogToday -> {
                            val date = Calendar.getInstance()
                            val year = date.get(Calendar.YEAR).toString()
                            val month = date.get(Calendar.MONTH).toString().padStart(2, '0')
                            val day = date.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            showSalesLog("$year$month$day")
                            true
                        }
                        R.id.salesLogYesterday -> {
                            val yesterday = Calendar.getInstance()
                            yesterday.add(Calendar.DATE, -1)
                            val year = yesterday.get(Calendar.YEAR).toString()
                            val month = yesterday.get(Calendar.MONTH).toString().padStart(2, '0')
                            val day = yesterday.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            showSalesLog("$year$month$day")
                            true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.info_log)
            popupMenu.show()
            */
        }

        //checkPermissions()
        sendBetsButton = findViewById(R.id.sendBetsButton)
        sendBetsButton.setOnClickListener {

            if (betList.size > 0) {
                confirm(calendar, storage)
            } else {
                Toast.makeText(this,"No bets to send.",Toast.LENGTH_LONG).show()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            // Request the user to grant permission to read SMS messages
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 2)
            System.out.println("Permission Denied")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH), PERMISSION_BLUETOOTH)
        }

    }

    private fun checkPermissions() {
        val permission1 =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION,1)
        }
    }

    private fun inputTarpCode(storage: SharedPreferences) {
        val inflter = LayoutInflater.from(this)
        val t = inflter.inflate(R.layout.register_booth,null)
        val editTextTarpCode = t.findViewById<EditText>(R.id.editTextTextTarpCode)
        val addDialog = AlertDialog.Builder(this)
            .setView(t)
            .setPositiveButton("Accept") {
                    dialog, _->
                dialog.dismiss()
            }
        .show()
        addDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))

        val okButton: Button = addDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            var tarpCode = editTextTarpCode.text.toString()
            if (tarpCode.isNullOrEmpty()) {
                Toast.makeText(this,"12 character Tarp Code is required.",Toast.LENGTH_LONG).show()
            } else if (tarpCode.length != 12) {
                Toast.makeText(this,"Tarp Code should be 12 characters long. You entered ${tarpCode.length}.",Toast.LENGTH_LONG).show()
            } else {
                if (!tarpCode.isNullOrEmpty()) {
                    var editor = storage.edit()
                    editor.putString(TARP_CODE, tarpCode)
                    editor.apply()
                }
                addDialog.dismiss()
            }
        }
    }

    private fun addBets(storage: SharedPreferences) {
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.add_item,null)

        /**set view*/
        val inputCombination = v.findViewById<EditText>(R.id.inputCombination)
        val inputStraight = v.findViewById<EditText>(R.id.inputStraight)
        val inputRambol = v.findViewById<EditText>(R.id.inputRambol)

        inputStraight.filters = arrayOf<InputFilter>(MinMaxFilter(0, 50))
        inputRambol.filters = arrayOf<InputFilter>(MinMaxFilter(0, 50))

        val addDialog = AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton("Ok") { _, _->  }
            .setNegativeButton("Cancelled.") {
                    dialog,_->
                dialog.dismiss()
                Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show()

            }
            .show()
        addDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))

        val okButton: Button = addDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            val combination = inputCombination.text.toString()
            val straight = inputStraight.text.toString()
            val rambol = inputRambol.text.toString()

            inputCombination.setTextColor(Color.BLACK)
            inputStraight.setTextColor(Color.BLACK)
            inputRambol.setTextColor(Color.BLACK)

            if (combination.length < 3) {
                inputCombination.setTextColor(Color.RED)
                inputCombination.blink(5)
                Toast.makeText(this, "Combination should be 3 digits long", Toast.LENGTH_SHORT).show()
            } else if (straight.isEmpty()) {
                inputStraight.setTextColor(Color.RED)
                inputStraight.blink(5)
                Toast.makeText(this, "Please provide amount for Straight bet", Toast.LENGTH_SHORT).show()
            } else if (rambol.isEmpty()) {
                inputRambol.setTextColor(Color.RED)
                inputRambol.blink(5)
                Toast.makeText(this, "Please provide amount for Rambol bet", Toast.LENGTH_SHORT).show()
            } else if (straight.toInt() == 0 && rambol.toInt() == 0) {
                inputStraight.setTextColor(Color.RED)
                inputStraight.blink(5)
                inputRambol.setTextColor(Color.RED)
                inputRambol.blink(5)
                Toast.makeText(this, "Please provide amount more than 0 for Straight and Rambol", Toast.LENGTH_SHORT).show()
            } else if ((array_HOT_NUMBERS.contains(combination) || isDailyHotNumber(combination)) && straight.toInt() > 0)  {
                inputCombination.blink(5)
                Toast.makeText(applicationContext,"$combination is a Hot number.\n Rambol bet only. Straight bet set to 0", Toast.LENGTH_SHORT).show()
                inputStraight.setText("0")
            } else {
                betList.add(UserData(combination, straight, rambol))
                if (betList.size > 0) {
                    findViewById<TextView>(R.id.Combi).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.Straight).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.Rambol).visibility = View.VISIBLE
                }
                userAdapter.notifyDataSetChanged()
                addDialog.dismiss()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showInfo(calendar: Calendar, storage: SharedPreferences) {
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        if (storage.getInt(DAY_OF_MONTH, 0) != today) {  //check if it is another day
            ResetDrawSales_UpdateCurrentDate(calendar, storage)
            deleteLogDate(2)
        }
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.salesinfo_item,null)
        val versionNum = v.findViewById<TextView>(R.id.versionNum)
        val textViewTarpCode = v.findViewById<TextView>(R.id.tarpNum)
        versionNum.text = "Version: $VERSION"
        val tarpCode = storage.getString(TARP_CODE, "")
        textViewTarpCode.text = "Tarp Code: " + tarpCode.toString().uppercase()
        val sdf = SimpleDateFormat("M/dd/yyyy hh:mm a")
        val currentDate = sdf.format(Date())
        val salesText = v.findViewById<TextView>(R.id.TarpCodeLabel)
        if (storage.getInt(DAY_OF_MONTH, 0) != today) {
            val yesterday = calendar.get(Calendar.DAY_OF_MONTH) - 1
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            salesText.setText("Sales from yesterday " + month + "/" + yesterday + "/" + year)
        } else {
            salesText.setText("Sales for today as of \n" + currentDate)
        }
        val salesData = v.findViewById<TextView>(R.id.salesData)

        salesData.setText(
                "\n2 PM Draw: P " + GetDrawTimeSales(DRAW_TIME_2PM, storage).toString() + ".00 \n" +
                "5 PM Draw: P " + GetDrawTimeSales(DRAW_TIME_5PM, storage).toString() + ".00 \n" +
                "9 PM Draw: P " + GetDrawTimeSales(DRAW_TIME_9PM, storage).toString() + ".00 \n" +
                        "--------------------------- \n" +
                "Total Sales: P " + GetTotalDrawTimeSales(storage).toString() + ".00 \n"
        )

        val addDialog = AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton("Ok") {
                    dialog, _->
                dialog.dismiss()

            }
            .show()
        addDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))
    }

    @SuppressLint("SetTextI18n")
    private fun showSalesLog(logDate: String) {

        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.day_sales_log,null)
        val salesLog = v.findViewById<TextView>(R.id.logContent)
        val fileName = "$DGLOG_PREFIX$logDate.txt"

        var file = File(applicationContext.filesDir, fileName)
        var content = ""
        if (file.exists()) {
            val bufferedReader = file.bufferedReader()
            val logs:List<String> = bufferedReader.readLines()
            if (!logs.isEmpty()) {
                var counter = 2
                for (line in logs) {
                        if (!line.isEmpty()) {
                            content += line
                            if (logs.size > counter) {
                                content += "\n"
                            }
                            counter++
                        }
                }
                content.trimIndent().trimMargin()
                salesLog.text = content + "\n\n"
            } else {
                salesLog.text = "No Logs available."
            }
        } else {
            salesLog.text = "No Logs available."
        }

        val addDialog = AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton("Ok") {
                    dialog, _->
                dialog.dismiss()

            }
            .show()
        addDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))
    }

    private fun AddSalesLog(bets: String, status: String) {
        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR).toString()
        val month = date.get(Calendar.MONTH).toString().padStart(2, '0')
        val day = date.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val hour = date.get(Calendar.HOUR_OF_DAY).toString()
        val minute = date.get(Calendar.MINUTE).toString()
        val seconds = date.get(Calendar.SECOND).toString()
        val logHead = "$year-$month-$day $hour:$minute:$seconds"
        var newLog = logHead + "\n" + bets
        if (status != "") {
            newLog += "\n" + status
        }

        val fileName = "$DGLOG_PREFIX$year$month$day.txt"
        var file = File(applicationContext.filesDir, fileName)
        var content = ""
        if (file.exists()) {
            val bufferedReader = file.bufferedReader()
            val logs:List<String> = bufferedReader.readLines()
            val newEntry = logs.plus(newLog)
            for (line in newEntry) {
                content += line + "\n"
            }
            File(applicationContext.filesDir, fileName).bufferedWriter().use { out ->
                out.write(content)
            }
        } else {
            File(applicationContext.filesDir, fileName).bufferedWriter()
                .use { out -> out.write(newLog) }
        }
    }

    private fun deleteLogDate(daysFromDeleteStart: Int) {
        for (x in 0..6) {  //wipe out 7 days of Logs
            var calendarLog = Calendar.getInstance()
            var nthDay = daysFromDeleteStart + x
            calendarLog.add(Calendar.DAY_OF_YEAR, -nthDay)
            val year = calendarLog.get(Calendar.YEAR).toString()
            val month = calendarLog.get(Calendar.MONTH).toString().padStart(2, '0')
            val day = calendarLog.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val fileName = "$DGLOG_PREFIX$year$month$day.txt"
            val file = File(applicationContext.filesDir, fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun getBluetoothConnection() : BluetoothConnection? {
        var connection = BluetoothPrintersConnections.selectFirstPaired()
        if (connection == null) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH),
                    PERMISSION_BLUETOOTH
                )
            }
        }

        return connection
    }

    private fun confirm(calendar: Calendar, storage: SharedPreferences) {
        var betMessage = ""
        val bluetoothConnection = getBluetoothConnection()
        if (bluetoothConnection != null) {
            val inflter = LayoutInflater.from(this)
            val v_confirm = inflter.inflate(R.layout.confirmation_item, null)
            val progressBar =  v_confirm.findViewById<ProgressBar>(R.id.progressBar)
            progressBar.visibility = View.GONE
            var message = ""
            var sales = 0
            for (element in betList) {
                message += element.dataCombination + " . " + element.dataStraight + " . " + element.dataRambol + "\n"
                sales += element.dataStraight.toInt() + element.dataRambol.toInt()
            }
            val salesHeader = v_confirm.findViewById<TextView>(R.id.textSales)
            val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm")
            val currentDate = sdf.format(Date())
            salesHeader.setText("Please confirm bets placed for " + getDrawTime() + " on " + currentDate)

            val lineBets = v_confirm.findViewById<TextView>(R.id.TextConfirmBets)
            lineBets.text = message
            val totalSales = v_confirm.findViewById<TextView>(R.id.textViewTotalSale)

            totalSales.text = "Total Bet Amount:  P " + sales + ".00"

            val confirmDialog = AlertDialog.Builder(this)
                .setView(v_confirm)
                .setPositiveButton("Send") { _, _ -> }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
                }
                .show()
            confirmDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))

            val okButton: Button = confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (cutOffTime(calendar)) {
                    if (getDrawTime() == "9PM") {
                        Toast.makeText(this,"Cut off time. Betting opens tomorrow at 6AM.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this,"Cut off time. Betting opens 10 minutes after " + getDrawTime() + " draw.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    //send sms here
                    progressBar.visibility = View.VISIBLE
                    var phoneNumber =  SMALL_BETS_SMART1
                    var salesSend = 0
                    var counter = 1
                    for (element in betList) {
                        //var phoneNumber = getSMSNumberToSend(element)
                        //var betMessage = element.dataCombination+"."+element.dataStraight+"."+element.dataRambol
                        var lineBet = element.dataCombination + "." + element.dataStraight + "." + element.dataRambol
                        if (betList.size > counter) {
                            lineBet += "\n"
                        }

                        betMessage +=  lineBet
                        salesSend += element.dataStraight.toInt() + element.dataRambol.toInt()
                        counter++
                    }

                    try {
                        //val smsManager: SmsManager = SmsManager.getDefault()
                        //smsManager.sendTextMessage(phoneNumber, null, betMessage, null,null)
                        Toast.makeText(this,"Sending..........", Toast.LENGTH_LONG).show()
                        sendSMSIntent(phoneNumber, betMessage, sales, currentDate, salesSend, calendar, bluetoothConnection, storage)
/*
                        printReceipt(
                            betList,
                            phoneNumber,
                            sales,
                            currentDate,
                            bluetoothConnection,
                            storage
                        )
                        AddSalestoDrawTime(salesSend, calendar, storage)
                        betList.clear()
                        recv?.adapter?.notifyDataSetChanged()
*/
                        confirmDialog.dismiss()
                        progressBar.visibility = View.GONE
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG)
                            .show()
                        //status = "Sending SMS error! Submission Aborted. " + e.toString()
                    }
                    //AddSalesLog(betMessage, status)
                }
            }
        } else {
            Toast.makeText(this, "No printer connected!", Toast.LENGTH_SHORT).show()
            //status += "Printing Error! Submission Aborted."
        }
        if (!betMessage.isNullOrEmpty()) {
            //AddSalesLog(betMessage, status)
        }
    }

    private fun sendSMSIntent(
        phoneNumber: String,
        message: String,
        sales: Int,
        currentDate: String,
        salesSend: Int,
        calendar: Calendar,
        bluetoothConnection: BluetoothConnection,
        storage: SharedPreferences
    ) {
        // Intent Filter Tags for SMS SEND and DELIVER
        val SENT = "SMS_SENT";
        val DELIVERED = "SMS_DELIVERED";
// STEP-1___
        // SEND PendingIntent
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(
        SENT), 0);

        // DELIVER PendingIntent
        val deliveredPI = PendingIntent.getBroadcast(this, 0,
        Intent(DELIVERED), 0);
// STEP-2___
        // SEND BroadcastReceiver
        val sendSMS = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                when ( getResultCode()) {
                    Activity.RESULT_OK -> {
                        //Toast.makeText(getBaseContext(), "SMS sent.", Toast.LENGTH_SHORT).show();
                    }
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                        Toast.makeText(getBaseContext(),"SMS Sending Failed. Code(GF).", Toast.LENGTH_SHORT).show();
                    }
                    SmsManager.RESULT_ERROR_NO_SERVICE -> {
                        Toast.makeText(getBaseContext(), "SMS Sending Failed: No service.", Toast.LENGTH_SHORT).show();
                    }
                    SmsManager.RESULT_ERROR_NULL_PDU -> {
                        Toast.makeText(getBaseContext(), "SMS Sending Failed: Code:(NP).", Toast.LENGTH_SHORT).show();
                    }
                    SmsManager.RESULT_ERROR_RADIO_OFF -> {
                        Toast.makeText(getBaseContext(), "SMS Sending Failed. Code(RO).", Toast.LENGTH_SHORT).show();
                    }
                    else -> {
                        Toast.makeText(getBaseContext(), "SMS Sending Failed. No Load.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // DELIVERY BroadcastReceiver
        val deliverSMS = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                when (getResultCode()) {
                    Activity.RESULT_OK -> {
                        if (betList.size > 0) {
                            Toast.makeText(getBaseContext(), "Bets delivered.", Toast.LENGTH_SHORT)
                                .show();
                            printReceipt(
                                betList,
                                phoneNumber,
                                sales,
                                currentDate,
                                bluetoothConnection,
                                storage
                            )
                            AddSalestoDrawTime(salesSend, calendar, storage)
                            betList.clear()
                            recv?.adapter?.notifyDataSetChanged()
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(getBaseContext(), "Bets NOT delivered.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
// STEP-3___
        try {
            // ---Notify when the SMS has been sent---
            registerReceiver(sendSMS, IntentFilter(SENT));
            // ---Notify when the SMS has been delivered---
            registerReceiver(deliverSMS, IntentFilter(DELIVERED));

            val sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        } catch (e: java.lang.Exception) {
            //AddSalesLog("status:", e.toString())
        }
    }


    private fun getDrawTime() : String {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)

        return when {
            hour < 14 -> DRAW_TIME_2PM
            hour < 17 -> DRAW_TIME_5PM
            else -> DRAW_TIME_9PM
        }
    }

    private fun AddSalestoDrawTime(drawSales: Int, calendar: Calendar, storage: SharedPreferences) {
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        var editor = storage.edit()
        var drawTime: String
        when (getDrawTime()) {
            DRAW_TIME_2PM -> drawTime = DRAW_TIME_2PM
            DRAW_TIME_5PM -> drawTime = DRAW_TIME_5PM
            else -> drawTime = DRAW_TIME_9PM
        }

        if (storage.getInt(DAY_OF_MONTH, 0) != today) {
            ResetDrawSales_UpdateCurrentDate(calendar, storage)
            deleteLogDate(2)
        }
        editor.putInt(drawTime, storage.getInt(drawTime, 0) + drawSales)

        editor.apply()
    }

    private fun GetDrawTimeSales(drawTime: String, storage: SharedPreferences) : Int {
        return storage.getInt(drawTime, 0)
    }

    private fun GetTotalDrawTimeSales(storage: SharedPreferences) : Int {
        return storage.getInt(DRAW_TIME_2PM, 0) + storage.getInt(DRAW_TIME_5PM, 0) + storage.getInt(DRAW_TIME_9PM, 0)
    }

    private fun ResetDrawSales_UpdateCurrentDate(calendar: Calendar, storage: SharedPreferences) {
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        if (storage.getInt(DAY_OF_MONTH, 0) != today) {
            var editor = storage.edit()
            editor.putInt(DRAW_TIME_2PM, 0)
            editor.putInt(DRAW_TIME_5PM, 0)
            editor.putInt(DRAW_TIME_9PM, 0)
            editor.putInt(DAY_OF_MONTH, today)
            editor.apply()
        }
    }

    private fun getSMSNumberToSend(userData: UserData) : String {
        val HIGH = 1
        val LOW = 2
        var highLowGroupNumber: Int

        if (userData.dataStraight.toInt() >= 40 ||
            isDoubleNumbers(userData.dataCombination) || isTripleNumbers(userData.dataCombination) ||
            array_HOT_NUMBERS.contains(userData.dataCombination)) {
            highLowGroupNumber = HIGH
        } else {
            highLowGroupNumber = LOW
        }

        if (highLowGroupNumber == HIGH) {
            return if (currentNetwork == NETWORK_SMART) {
                HIGH_BETS_SMART
            } else {
                HIGH_BETS_GLOBE
            }
        } else {
            val rands = (0..1).random()
            return if (currentNetwork == NETWORK_SMART) {
                if (rands == 0) {
                    SMALL_BETS_SMART1
                } else {
                    SMALL_BETS_SMART2
                }
            } else {
                if (rands == 0) {
                    SMALL_BETS_GLOBE1
                } else {
                    SMALL_BETS_GLOBE2
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun printReceipt(betList: ArrayList<UserData>, phoneNumber: String, sales: Int, currentDate: String, connection: BluetoothConnection?, storage: SharedPreferences) {
        var text = ""
        try {
            if (connection != null) {
                val drawDate = SimpleDateFormat("MMM dd, yyyy hh:mm a")

                val printer = EscPosPrinter(connection, 203, 48f, 32)
                text =  "            3D Games\n" +
                            "           SGC GAMING\n" +
                            "\n" +
                            "Draw Date: " + drawDate.format(Date()) + "\n" +
                            "Draw time: " + getDrawTime() + "\n" +
                            "Ref. No:" + generateRefNum(currentDate, phoneNumber, sales, betList.size, storage) + "\n" +
                            "Combination  Straight  Rambol \n"

                for (element in betList) {
                    text += element.dataCombination + "           " +
                            element.dataStraight.padStart(2) + "         " +
                            element.dataRambol.padStart(2) + "\n"
                }

                text += "\n"
                text += "Total Amount: $sales \n"
                text += "Total Bets: ${betList.size}\n"
                text += "WEB"
                printer.printFormattedText(text)
                printer.disconnectPrinter()
                Toast.makeText(this, "Receipt printed.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No printer connected.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("APP", "Can't print", e)
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateRefNum(currentDate: String, phoneNumber: String, betsAmount: Int, betsTotal: Int, storage: SharedPreferences) : String {
        /*
        var calendar = Calendar.getInstance()
        var sec = calendar.get(Calendar.SECOND)
        var rand = sec % 10

        when (rand) {
            0,1 -> dgPattern = "S2X67"
            2,3 -> dgPattern = "L7D54"
            4,5 -> dgPattern = "Q8C19"
            6,7 -> dgPattern = "H1J34"
            8,9 -> dgPattern = "E4W98"
        }
        */

        var dgPattern = "DG"
        val tarpCode = storage.getString(TARP_CODE, "")?.uppercase()

        return currentDate.substring(8, 10) + currentDate.substring(0, 2) + currentDate.substring(3, 5) +
                currentDate.substring(11, 13) + currentDate.substring(14, 16) +
                dgPattern +
                //betsAmount.toString().padStart(3, '0') +
                //betsTotal.toString().padStart(2, '0') +
                //phoneNumber.substring(9) +
                tarpCode
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null,null)
            Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun isTripleNumbers(combination: String) : Boolean {
        return combination[0] == combination[1] && combination[1] == combination[2]
    }

    private fun isDoubleNumbers(combination: String) : Boolean {
        return (combination[0] == combination[1] && combination[1] != combination[2]) ||
                (combination[0] == combination[2] && combination[0] != combination[1]) ||
                (combination[1] == combination[2] && combination[0] != combination[1])
    }

    private fun isDailyHotNumber(combination: String) : Boolean {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val currentDate = sdf.format(Date())
        val month = currentDate.toString().substring(0,2).toInt()
        return if (month < 10) {
            val today = currentDate.toString().substring(3,5)
            val monthDay = month.toString() + today
            monthDay.trim() == combination
        } else {
            false
        }
    }

    private fun cutOffTime(calendar: Calendar) : Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        return (((hour == 13 || hour == 16 || hour == 20) && minutes in (50..59)) || ((hour == 14 || hour == 17 || hour == 21) && minutes in (0..10)))
    }

    // Custom class to define min and max for the edit text
    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        // Initialized
        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        // Check if input c is in between min a and max b and
        // returns corresponding boolean
        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    fun View.blink(
        times: Int = Animation.INFINITE,
        duration: Long = 50L,
        offset: Long = 20L,
        minAlpha: Float = 0.0f,
        maxAlpha: Float = 1.0f,
        repeatMode: Int = Animation.REVERSE
    ) {
        startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
            it.duration = duration
            it.startOffset = offset
            it.repeatMode = repeatMode
            it.repeatCount = times
        })
    }

}