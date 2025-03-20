package com.ingenieriajhr.pascalandroid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ingenieriajhr.blujhr.BluJhr
import com.ingenieriajhr.pascalandroid.databinding.ActivityMainBinding
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    lateinit var vb: ActivityMainBinding
    lateinit var blue:BluJhr
    var devicesBluetooth = ArrayList<String>()


    var estadoConexion = BluJhr.Connected.False

    var isPermissionSoli = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //init blu Jhr
        blue = BluJhr(this)
        blue.onBluetooth()

        changeListDevice()


        //list device
        vb.listDeviceBluetooth.setOnItemClickListener { adapterView, view, i, l ->
            if (devicesBluetooth.isNotEmpty()){
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object:BluJhr.ConnectedBluetooth{
                    override fun onConnectState(state: BluJhr.Connected) {
                        when(state){

                            BluJhr.Connected.True->{
                                Toast.makeText(applicationContext,"Conectado",Toast.LENGTH_SHORT).show()
                                changeDashboard()
                                rxReceived()
                            }

                            BluJhr.Connected.Pending->{
                                Toast.makeText(applicationContext,"Pendiente",Toast.LENGTH_SHORT).show()

                            }

                            BluJhr.Connected.False->{
                                Toast.makeText(applicationContext,"Falso",Toast.LENGTH_SHORT).show()
                                changeListDevice()

                            }

                            BluJhr.Connected.Disconnect->{
                                Toast.makeText(applicationContext,"Desconectado",Toast.LENGTH_SHORT).show()
                                changeListDevice()
                            }

                        }
                    }
                })
            }
        }


    }

    /**
     * Change to list device
     */
    private fun changeListDevice(){
        vb.viewDevice.visibility = View.VISIBLE
        vb.viewDashboard.visibility = View.GONE
    }

    /**
     * Change to dashboard
     */
    private fun changeDashboard(){
        vb.viewDevice.visibility = View.GONE
        vb.viewDashboard.visibility = View.VISIBLE
    }



    /**
     * Rx Data received
     */
    private fun rxReceived() {
        blue.loadDateRx(object : BluJhr.ReceivedData {
            override fun rxDate(rx: String) {
                runOnUiThread {
                    vb.txtConsole.text = "Datos de llegada "+rx



                    //if rx is number
                    if (rx.toIntOrNull() != null){
                        vb.viewPascal.setPressure(rx.toFloat())
                    }
                }
            }
        })
    }


    /**
     * Calcular la presión a partir del voltaje
     * @param voltaje el voltaje medido
     * @return la presión calculada
     */
    fun calculatePressure(voltaje: Float): Float {
        //delta x
        val deltaX = (-0.26+ sqrt((0.0676-(1.6*(1.12-voltaje)))))/(0.8)
        //calculate pressure
        return (deltaX*714.3).toFloat()
    }


    /**
    * pedimos los permisos correspondientes, para android 12 hay que pedir los siguientes admin y scan
    * en android 12 o superior se requieren permisos diferentes
    */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       // blue.initializeBluetooth()
        if (!isPermissionSoli){
            isPermissionSoli = true
            Log.d("PERMISOS","Entramos en on request")
            if (blue.checkPermissions(requestCode,grantResults)){
                Log.d("PERMISOS","Entramos en on 2")
                Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
                blue.initializeBluetooth()
            }else{
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                    Log.d("PERMISOS","Entramos en on 3")
                    blue.initializeBluetooth()
                }else{
                    Log.d("PERMISOS","Entramos en on 4")

                    Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    vb.listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }

            }
        }



        super.onActivityResult(requestCode, resultCode, data)
    }



}