package xyz.techrelation.numberedticket

import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.sunmi.peripheral.printer.*
import java.text.SimpleDateFormat
import java.time.Instant.now
import java.time.LocalDate
import java.time.chrono.JapaneseDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var sunmiPrinterService: SunmiPrinterService

    var NoSunmiPrinter = 0x00000000
    var CheckSunmiPrinter = 0x00000001
    var FoundSunmiPrinter = 0x00000002
    var LostSunmiPrinter = 0x00000003
    var sunmiPrinter = CheckSunmiPrinter

    private lateinit var queueNumberView: EditText
    private lateinit var resetButton: Button
    private lateinit var printButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSunmiPrinterService(this)

        queueNumberView = this.findViewById(R.id.queueNumberView)
        printButton = this.findViewById(R.id.printButton)
        resetButton = this.findViewById(R.id.resetButton)


        val startNumber: Int = 0
        var queueNumber: Int
        var queueNumber_st: String


        printButton.setOnClickListener {

            //print:headerDesign
            sunmiPrinterService.setFontSize(24.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(0, null)
            printText("🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡"+ "\n")

            //print:nowDate
            val now = Date()
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val result = sdf.format(now)
            sunmiPrinterService.setFontSize(30.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(2, null)
            printText(result + "\n")


            //print:queueNumber
            queueNumber_st = queueNumberView.getText().toString()
            queueNumber = queueNumber_st.toInt()
            sunmiPrinterService.setFontSize(96.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(1, null)
            printText(queueNumber_st + "\n")

            //print:Message
            sunmiPrinterService.setFontSize(30.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(1, null)
            printText("会計時にお渡しください。"+ "\n")

            /* print:Detail
            sunmiPrinterService.setAlignment(0, null)
            printText("薄皮たい焼き（120円）×0"+ "\n")
            printText("黒たい焼き（130円）×2"+ "\n")
            printText("だんご（120円）×1"+ "\n")
            printText("ぜんざい（500円）×0"+ "\n")
            printText("合計380円") */

            //print:footerDesign
            sunmiPrinterService.setFontSize(24.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(0, null)
            printText("🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡"+ "\n")

            feedPaper(10)

            //autoUpdate:queueNumber
            queueNumber++
            queueNumber_st = queueNumber.toString()
            queueNumberView.setText(queueNumber_st)
        }

        resetButton.setOnClickListener {
            queueNumber = startNumber
            queueNumber_st = queueNumber.toString()
            queueNumberView.setText(queueNumber_st)
        }


    }
    private fun printText(queueNumber_st: String) {
        try {
            sunmiPrinterService.printText(queueNumber_st, object : InnerResultCallback() {
                @Throws(RemoteException::class)
                override fun onRunResult(isSuccess: Boolean) {
                }

                @Throws(RemoteException::class)
                override fun onReturnString(result: String) {
                }

                @Throws(RemoteException::class)
                override fun onRaiseException(code: Int, msg: String) {
                }

                @Throws(RemoteException::class)
                override fun onPrintResult(code: Int, msg: String) {
                }
            })
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun feedPaper(n :Int) {
        try {
            sunmiPrinterService.lineWrap(n, object : InnerResultCallback() {
                @Throws(RemoteException::class)
                override fun onRunResult(isSuccess: Boolean) {
                }

                @Throws(RemoteException::class)
                override fun onReturnString(result: String) {
                }

                @Throws(RemoteException::class)
                override fun onRaiseException(code: Int, msg: String) {
                }

                @Throws(RemoteException::class)
                override fun onPrintResult(code: Int, msg: String) {
                }
            })
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private val innerPrinterCallback: InnerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            sunmiPrinterService = service
            checkSunmiPrinterService(service)
        }

        override fun onDisconnected() {
            sunmiPrinter = LostSunmiPrinter
        }
    }

    private fun checkSunmiPrinterService(service: SunmiPrinterService) {
        var ret = false
        try {
            ret = InnerPrinterManager.getInstance().hasPrinter(service)
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }
        sunmiPrinter = if (ret) FoundSunmiPrinter else NoSunmiPrinter
    }

    fun initSunmiPrinterService(context: Context?) {
        try {
            val ret = InnerPrinterManager.getInstance().bindService(
                context,
                innerPrinterCallback
            )
            if (!ret) {
                sunmiPrinter = NoSunmiPrinter
            }
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }
    }


}