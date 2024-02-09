package xyz.techrelation.numberedticket


import android.Manifest.permission_group.CAMERA
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
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
import java.util.Arrays.toString


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
    private lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_main)
        initSunmiPrinterService(this)

        queueNumberView = this.findViewById(R.id.queueNumberView)
        printButton = this.findViewById(R.id.printButton)
        resetButton = this.findViewById(R.id.resetButton)
        scanButton = this.findViewById(R.id.scanButton)


        val startNumber: Int = 0
        var queueNumber: Int
        var queueNumber_st: String



        printButton.setOnClickListener {

            //print:headerDesign
            sunmiPrinterService.setFontSize(24.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(0, null)
            printText("🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡" + "\n")

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
            printText("会計時にお渡しください。" + "\n")


            //print:footerDesign
            sunmiPrinterService.setFontSize(24.0F, null)
            //0->left 1->center 2->right
            sunmiPrinterService.setAlignment(0, null)
            printText("🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡🐡🍡" + "\n")

            feedPaper(1)
            //print:UUID-QR
            val uuidString = UUID.randomUUID().toString()
            Log.i("generatedUUID", uuidString)
            val qrImage = createQrCode(uuidString)

            if (qrImage != null) {

                printBitmap(qrImage)
            }

            /* For debug
            sunmiPrinterService.setAlignment(0, null)
            printText("\n"+uuidString)*/

            feedPaper(5)

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

        scanButton.setOnClickListener{
            val qrScanIntegrator = IntentIntegrator(this)
            qrScanIntegrator.setOrientationLocked(true)
            qrScanIntegrator.setBeepEnabled(false)
            qrScanIntegrator.initiateScan()

        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(resultCode,data)
        if(result.contents != null){
            Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()

            AlertDialog.Builder(this)
                .setTitle("合計380円(9999)")
                .setMessage("番茶たい焼き（130円）×2→260円\nだんご（120円）×1→120円")
                .setPositiveButton("OK") { dialog,  id ->

                }
                .setNegativeButton("Cancel") {dialog, id ->

                }

                .show()

        }
    }

    private fun printText(pritText: String) {
        try {
            sunmiPrinterService.printText(pritText, object : InnerResultCallback() {
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

    private fun printBitmap(pritBitmap: Bitmap){
        try {
            sunmiPrinterService.printBitmap(pritBitmap, object : InnerResultCallback() {
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

    private fun feedPaper(n: Int) {
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

    fun createQrCode(data: String): Bitmap? {
        return try {
            val bitMatrix = createBitMatrix(data)
            bitMatrix?.let { createBitmap(it) }
        } catch (e: Exception) {
            Log.e("createQR", "${e.message}\n${e.stackTrace.joinToString("\n")}")
            null
        }
    }

    fun createBitMatrix(data: String): BitMatrix? {
        val multiFormatWriter = MultiFormatWriter()
        val hints = mapOf(
            EncodeHintType.MARGIN to 0,
            // setErrorCorrectionLevel
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q
        )

        return multiFormatWriter.encode(
            data,
            BarcodeFormat.QR_CODE,
            150,
            150,
            hints // option
        )


    }

    fun createBitmap(bitMatrix: BitMatrix): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }
}