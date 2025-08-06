package com.owenapps.stldg

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.owenapps.stldg.model.UserData
import com.owenapps.stldg.view.UserAdapter
import java.text.SimpleDateFormat
import java.util.*

class MainActivity_backup_myPhone : AppCompatActivity() {
    private lateinit var addBetsBtn:Button
    private lateinit var recv:RecyclerView
    private lateinit var betList:ArrayList<UserData>
    private lateinit var userAdapter:UserAdapter
    private lateinit var sendBetsButton: Button
    private lateinit var infoButton: ImageButton
    private var HIGH_BETS_TNT = "+639638749595"
    private var HIGH_BETS_GLOBE = "+639368306293"
    private var SMALL_BETS_TNT1 = "+639195698921"
    private var SMALL_BETS_TNT2 = "+639424872464"
    private var SMALL_BETS_GLOBE1 = "+639759111579"
    private var SMALL_BETS_GLOBE2 ="+639974152020"
    private val DRAW_TIME_2PM = "2pmDraw"
    private val DRAW_TIME_5PM = "5pmDraw"
    private val DRAW_TIME_9PM = "9pmDraw"
    private val DAY_OF_MONTH = "dayOfMonth"
    private val NETWORK_SMART = 1
    private val NETWORK_GLOBE = 2
    private var currentNetwork = NETWORK_GLOBE
    private val array_HOT_NUMBERS = arrayOf("123","213","231","312","132","321","120","201","102","502","250","104","125","129",
        "138","143","163","513", "122","221","212","242","224","422","112","212","131","113",
        "424","244","101","110","525","220","100","500","117","214","202","103","119")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 21) {
                Toast.makeText(applicationContext,"Betting is CLOSED for the day!.\n See you tomorrow.", Toast.LENGTH_SHORT).show()
            } else {
                addBets()
            }
        }

        infoButton.setOnClickListener {
            showInfo(calendar, storage)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            // Request the user to grant permission to read SMS messages
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 2)
            System.out.println("Permission Denied")
        }

        sendBetsButton = findViewById(R.id.sendBetsButton)
        sendBetsButton.setOnClickListener {

            if (betList.size > 0) {
                confirm(calendar, storage)
            }
        }

        //UpdateCurrentDate(calendar, storage )
    }

    private fun addBets() {
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
            .setNegativeButton("Cancel") {
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
                } else if (array_HOT_NUMBERS.contains(combination) && straight.toInt() > 0) {
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
                    Toast.makeText(this, "Adding of Bets Success", Toast.LENGTH_SHORT).show()
                    addDialog.dismiss()
                }
            }
    }

    private fun showInfo(calendar: Calendar, storage: SharedPreferences) {
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val inflter = LayoutInflater.from(this)
        val v = inflter.inflate(R.layout.salesinfo_item,null)
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

    private fun confirm(calendar: Calendar, storage: SharedPreferences) {
        val inflter = LayoutInflater.from(this)
        val v_confirm = inflter.inflate(R.layout.confirmation_item,null)

        var message = ""
        var sales = 0
        for (element in betList) {
            message += element.dataCombination+" . "+element.dataStraight+" . "+element.dataRambol+"\n"
            sales += element.dataStraight.toInt() + element.dataRambol.toInt()
        }
        val salesHeader = v_confirm.findViewById<TextView>(R.id.textSales)
        val sdf = SimpleDateFormat("M/dd/yyyy hh:mm a")
        val currentDate = sdf.format(Date())
        salesHeader.setText("Please confirm bets placed for " + getDrawTime() + " on " + currentDate )

        val lineBets = v_confirm.findViewById<TextView>(R.id.TextConfirmBets)
        lineBets.text = message
        val totalSales = v_confirm.findViewById<TextView>(R.id.textViewTotalSale)

        totalSales.text = "Total Bet Amount:  P " + sales + ".00"

        val confirmDialog = AlertDialog.Builder(this)
            .setView(v_confirm)
            .setPositiveButton("Confirm") { _, _->  }
            .setNegativeButton("Cancel") {
                    dialog,_->
                dialog.dismiss()
                Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show()

            }
            .show()
        confirmDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))

        val okButton: Button = confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            if (1 == 3) {
                Toast.makeText(this, "ERROR ", Toast.LENGTH_SHORT).show()
            } else {
                //send sms here
                for (element in betList) {
                    var phoneNumber = getSMSNumberToSend(element)
                    var betMessage = element.dataCombination+"."+element.dataStraight+"."+element.dataRambol
                    sendSMS(phoneNumber, betMessage)
                    Toast.makeText(this, "Sending " + betMessage + " to " + phoneNumber, Toast.LENGTH_SHORT).show()
                    message += element.dataCombination+" . "+element.dataStraight+" . "+element.dataRambol+"\n"
                    sales += element.dataStraight.toInt() + element.dataRambol.toInt()
                }
                AddSalestoDrawTime(sales, calendar, storage)
                confirmDialog.dismiss()
            }

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
                HIGH_BETS_TNT
            } else {
                HIGH_BETS_GLOBE
            }
        } else {
            val rands = (0..1).random()
            return if (currentNetwork == NETWORK_SMART) {
                if (rands == 0) {
                    SMALL_BETS_TNT1
                } else {
                    SMALL_BETS_TNT2
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

    fun sendSMS(phoneNumber: String, message: String) {
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