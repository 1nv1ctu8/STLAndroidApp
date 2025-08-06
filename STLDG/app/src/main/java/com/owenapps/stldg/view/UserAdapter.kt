package com.owenapps.stldg.view

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.owenapps.stldg.R
import com.owenapps.stldg.model.UserData

class UserAdapter(val c:Context,val betList:ArrayList<UserData>):RecyclerView.Adapter<UserAdapter.UserViewHolder>()
{

    inner class UserViewHolder(val v:View):RecyclerView.ViewHolder(v){
        var combination:TextView
        var straight:TextView
        var rambol:TextView
        var mMenus:ImageView

        init {
            combination = v.findViewById<TextView>(R.id.listCombination)
            straight = v.findViewById<TextView>(R.id.listStraight)
            rambol = v.findViewById<TextView>(R.id.listRambol)
            mMenus = v.findViewById(R.id.mMenus)
            mMenus.setOnClickListener { popupMenus(it) }
        }

        private fun popupMenus(v:View) {
            val position = betList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val v = LayoutInflater.from(c).inflate(R.layout.add_item,null)
                        v.findViewById<EditText>(R.id.inputCombination).setText(position.dataCombination)
                        v.findViewById<EditText>(R.id.inputStraight).setText(position.dataStraight)
                        v.findViewById<EditText>(R.id.inputRambol).setText(position.dataRambol)
                        val combination = v.findViewById<EditText>(R.id.inputCombination)
                        val straight = v.findViewById<EditText>(R.id.inputStraight)
                        val rambol = v.findViewById<EditText>(R.id.inputRambol)

                        val arrayHotNumbers = arrayOf("123","213","231","312","132","321","120","201","102","502","250","104","125","129",
                            "138","143","163","513", "122","221","212","242","224","422","112","212","131","113",
                            "424","244","101","110","525","220","100","500","117","214","202","103","119")

                        val editDialog = AlertDialog.Builder(c)
                            .setView(v)
                            .setPositiveButton("Ok"){
                                    dialog, _-> }
                            .setNegativeButton("Cancel"){
                                    dialog,_->
                                dialog.dismiss()

                            }
                            //.create()
                            .show()

                        val okButton: Button = editDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        okButton.setOnClickListener {
                            if (combination.text.toString().length < 3) {
                                combination.setTextColor(Color.RED)
                                combination.blink(5)
                                Toast.makeText(c, "Combination should be 3 digits long", Toast.LENGTH_SHORT).show()
                            } else if (straight.text.toString().isEmpty()) {
                                straight.blink(5)
                                Toast.makeText(c, "Please provide amount for Straight bet", Toast.LENGTH_SHORT).show();
                            } else if (rambol.text.toString().isEmpty()) {
                                rambol.blink(5)
                                Toast.makeText(c, "Please provide amount for Rambol bet", Toast.LENGTH_SHORT).show();
                            } else if (arrayHotNumbers.contains(combination.text.toString()) && straight.text.toString().toInt() > 0) {
                                combination.setTextColor(Color.BLACK)
                                straight.blink(5)
                                Toast.makeText(c,"${combination.text.toString()} is a Hot number.\n Rambol bet only. Straight bet set to 0", Toast.LENGTH_SHORT).show()
                                straight.setText("0")
                            } else {
                                position.dataCombination = combination.text.toString()
                                position.dataStraight = straight.text.toString()
                                position.dataRambol = rambol.text.toString()
                                notifyDataSetChanged()
                                Toast.makeText(c, "Line Bet was Edited", Toast.LENGTH_SHORT)
                                    .show()
                                editDialog.dismiss()
                            }
                        }
                        true
                    }
                    R.id.delete->{
                        /**set delete*/
                        AlertDialog.Builder(c)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("Are you sure to delete this Bet?")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                betList.removeAt(adapterPosition)
                                notifyDataSetChanged()
                                Toast.makeText(c,"Line Bet Deleted",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    else-> true
                }

            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_item,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val newList = betList[position]
        holder.combination.text = newList.dataCombination
        holder.straight.text = newList.dataStraight
        holder.rambol.text = newList.dataRambol
    }

    override fun getItemCount(): Int {
        return  betList.size
    }

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