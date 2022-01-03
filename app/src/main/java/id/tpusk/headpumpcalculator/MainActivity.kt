/**
 * @author Ahlillah
 * @since 1.0
 */

package id.tpusk.headpumpcalculator

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import id.tpusk.headpumpcalculator.databinding.ActivityMainBinding
import id.tpusk.headpumpcalculator.databinding.DialogTamplateBinding
import id.tpusk.headpumpcalculator.room.DataDB
import id.tpusk.headpumpcalculator.room.DataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


class MainActivity : AppCompatActivity() {

    private val db by lazy { DataDB(this) }
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val bindingTemplate by lazy { DialogTamplateBinding.inflate(layoutInflater) }
    private var dayaPump: Double = 0.0
    private var dayaPumpSave: String = ""
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initiationDialogSave()

        binding.btnSimpan.setOnClickListener {
            showSaveDialog()
        }

        binding.btnHasil.setOnClickListener {
            val g = 9.81 //variabel nilai gravitasi

            //inputan
            with(binding) {

                //validasi data
                when {
                    diameterPipa.text?.isBlank() == true -> diameterPipa.error = "masukkan nilai diameter"
                    debitAliran.text?.isBlank() == true -> debitAliran.error = "masukkan nilai debit"
                    ketinggian1.text?.isBlank() == true -> ketinggian1.error = "masukkan nilai ketinggian 1"
                    ketinggian2.text?.isBlank() == true -> ketinggian2.error = "masukkan nilai ketinggian 2"
                    panjangEkivalenPipa.text?.isBlank() == true -> panjangEkivalenPipa.error = "masukkan nilai panjang pipa"
                    efisiensi.text?.isBlank() == true -> efisiensi.error = "masukkan nilai efisiensi"
                    keranGerbang.text?.isBlank() == true -> keranGerbang.error = "masukkan jumlah keran gerbang"
                    keranBola.text?.isBlank() == true -> keranBola.error = "masukkan jumlah keran bola"
                    keranSudut.text?.isBlank() == true -> keranSudut.error = "masukkan jumlah keran sudut"
                    tekanan1.text?.isBlank() == true -> tekanan1.error = "masukkan nilai tekanan 1"
                    tekanan2.text?.isBlank() == true -> tekanan2.error = "masukkan nilai tekanan 2"
                    kecepatan1.text?.isBlank() == true -> kecepatan1.error = "masukkan nilai kecepatan 1"
                    kecepatan2.text?.isBlank() == true -> kecepatan2.error = "masukkan nilai kecepatan 2"
                    kerapatan.text?.isBlank() == true -> kerapatan.error = "masukkan nilai kerapatan"
                    viskositas.text?.isBlank() == true -> viskositas.error = "masukkan nilai viskositas"
                    belokan45.text?.isBlank() == true -> belokan45.error = "masukkan nilai belokan 45"
                    belokan90.text?.isBlank() == true -> belokan90.error = "masukkan nilai belokan 90"
                    diameterPipaBaru.text?.isBlank() == true -> diameterPipaBaru.error = "masukkan nilai diameter pipa baru"

                    //inisisasi data
                    else -> {
                        val diameterPipamm = diameterPipa.text.toString().toDouble()
                        val debitAliranh = debitAliran.text.toString().toDouble()
                        val ketinggian1 = ketinggian1.text.toString().toDouble()
                        val ketinggian2 = ketinggian2.text.toString().toDouble()
                        val panjangEkivalenPipa = panjangEkivalenPipa.text.toString().toDouble()
                        val efisiensi = efisiensi.text.toString().toDouble()
                        val keranGerbang = keranGerbang.text.toString().toDouble()
                        val keranBola = keranBola.text.toString().toDouble()
                        val keranSudut = keranSudut.text.toString().toDouble()
                        val tekanan1 = tekanan1.text.toString().toDouble()
                        val tekanan2 = tekanan2.text.toString().toFloat()
                        val kecepatanAliran1 = kecepatan1.text.toString().toDouble()
                        val kecepatanAliran2 = kecepatan2.text.toString().toDouble()
                        val kerapatan = kerapatan.text.toString().toDouble()
                        val viskositas = viskositas.text.toString().toDouble()
                        val belokan45 = belokan45.text.toString().toDouble()
                        val belokan90 = belokan90.text.toString().toDouble()
                        val diameterPipaBarumm = diameterPipaBaru.text.toString().toDouble()

                        val jenisAliran: String

                        val f: Double

                        //hitungan
                        val diameterPipa = diameterPipamm / 1000 //konversi mm ke m
                        val debitAliran = debitAliranh / 3600 //konversi jam ke detik
                        val r = diameterPipa / 2 //mengubah diameter ke jari jari
                        val a = (3.14 * r.pow(2)) //menetukan luas penampang pipa
                        val v = (debitAliran / a) //menentukan kecepatan aliran
                        val re = (kerapatan * v * diameterPipa) / viskositas //menentukan nilai reynold

                        Log.d(TAG, "luas penampang : $a")
                        Log.d(TAG, "kecepatan aliran : $v")
                        Log.d(TAG, "Re : $re")

                        jenisAliran = when { //menentukan jenis aliran
                            re < 2300 -> {
                                getString(R.string.laminer)
                            }
                            re in 2300f..4000f -> {
                                getString(R.string.transition)
                            }
                            else -> {
                                getString(R.string.turbulen)
                            }
                        }
                        Log.d(TAG, "Jenis aliran : $jenisAliran")

                        //friction
                        f = if (re < 2300) 64 / re else (0.3164 / re.pow(0.25)) //menentukan nilai koefisien gesekan
                        Log.d(TAG, "nilai f : $f")

                        //Entrance Length
                        val le = if (re < 2300) 138 * diameterPipa else 35 * diameterPipa //menentukan nilai length entrance
                        Log.d(TAG, "length entrance : $le")

                        //headloss keran gerbang
                        val ldg = 8 //konstanta rasio panjang pipa dengan diameter pipa untuk keran gerbang
                        val kHeadlossg = f * ldg //menentukan nilai koefisient headloss keran gerbang
                        Log.d(TAG, "kHeadlossg: $kHeadlossg")
                        val headlossg = f * ldg * v.pow(2) * keranGerbang / (2 * g) //menentukan nilai headloss keran gerbang
                        Log.d(TAG, "headlossg: $headlossg")

                        //headloss keran bola
                        val ldb = 340 //konstanta rasio panjang pipa dengan diameter pipa untuk keran bola
                        val kHeadlossb = f * ldb //menentukan nilai koefisient headloss keran bola
                        Log.d(TAG, "kHeadlossb: $kHeadlossb")
                        val headlossb = f * ldb * v.pow(2) * keranBola / (2 * g) //menentukan nilai headloss keran bola
                        Log.d(TAG, "headlossb: $headlossb")

                        //headloss keran sudut
                        val lds = 150 //konstanta rasio panjang pipa dengan diameter pipa untuk keran sudut
                        val kHeadlosss = f * lds //menentukan nilai koefisient headloss keran sudut
                        Log.d(TAG, "kHeadlosss: $kHeadlosss")
                        val headlosss = f * lds * v.pow(2) * keranSudut / (2 * g) //menentukan nilai headloss keran sudut
                        Log.d(TAG, "headlosss: $headlosss")

                        //Headloss belokan 90 deg
                        val ld90 = 30 //konstanta rasio panjang pipa dengan diameter pipa untuk belokan 90 derajat
                        val headloss90 = (f * ld90 * v.pow(2) * belokan90) / (2 * g) //menentukan nilai headloss belokan 90 derajat
                        Log.d(TAG, "headloss90 : $headloss90")

                        //Headloss belokan 45 deg
                        val ld45 = 16 //konstanta rasio panjang pipa dengan diameter pipa untuk belokan 45 derajat
                        val headloss45 =
                                (f * ld45 * v.pow(2) * belokan45) / (2 * g) //menentukan nilai headloss belokan 45 derajat
                        Log.d(TAG, "headloss45 : $headloss45")

                        //Perbandingan diameter
                        val diameterPipaBaru = diameterPipaBarumm / 1000 //konversi mm ke m
                        val perbandinganDiameter =
                                if (diameterPipaBaru <= diameterPipa) diameterPipaBaru.pow(2) / diameterPipa.pow(2) else diameterPipa.pow(2) / diameterPipaBaru.pow(
                                        2
                                ) //menentukan nilai perbandingan perubahan diameter
                        Log.d(TAG, "perbandingan_diameter : $perbandinganDiameter")

                        //koefisient diameter
                        val koefisientDiameter =
                                if (perbandinganDiameter <= 0.715) 0.4 * (1.25 - perbandinganDiameter) else 0.75 * (1 - perbandinganDiameter) //menentukan nilai koefisien headloss diameter
                        Log.d(TAG, "koefisient diameter : $koefisientDiameter")

                        //headloss mayor
                        val headlossMayor =
                                f * panjangEkivalenPipa * v.pow(2) / (2 * g * diameterPipa) //menentukan nilai headloss mayor
                        Log.d(TAG, "headloss mayor : $headlossMayor")

                        //headloss entrace
                        val ken = 0.5 //koefisient untuk headloss entrance
                        val headlosse = ken * v.pow(2) / (2 * g) //menenutkan nilai headloss entrance
                        Log.d(TAG, "headlosse: $headlosse")

                        //headloss keran total
                        val headlosskt = headlossg + headlossb + headlosss //menentukan nilai headloss keran total
                        Log.d(TAG, "headlosskt: $headlosskt")

                        //headloss belokan
                        val headlossBel = headloss45 + headloss90 //menentukan nilai headloss belokan total
                        Log.d(TAG, "headloss belokan: $headlossBel")

                        //headloss diameter
                        val headlossd = koefisientDiameter * v.pow(2) / (2 * g * diameterPipa) //menenutkan nilai headloss diameter
                        Log.d(TAG, "headlossd : $headlossd")

                        //headloss minor total
                        val headlossMt: Double = headlosse + headlosskt + headlossBel //menentukan nilai headloss minor total
                        Log.d(TAG, "headloss minor total: $headlossMt")

                        //headloss total sistem
                        val headlossTotal = headlossMayor + headlossMt //menentukan nilai headloss total pada sistem
                        Log.d(TAG, "headloss total: $headlossTotal")

                        //head pump total sistem
                        val headPump =
                                (ketinggian2 + (tekanan2 / (kerapatan * g)) + (kecepatanAliran2.pow(2) / (2 * g))) - (ketinggian1 + (tekanan1 / (kerapatan * g)) + (kecepatanAliran1.pow(
                                        2
                                ) / (2 * g))) + headlossTotal //menentukan nilai headloss total pafa sistem pompa
                        Log.d(TAG, "head pompa total: $headPump")

                        //daya pump total
                        dayaPump = kerapatan * g * debitAliran * headPump * (efisiensi / 100) //menentukan nilai daya pompa yang dibuthkan
                        Log.d(TAG, "daya pompa total: $dayaPump")

                        when (binding.spSatuan.selectedItemId) {
                            0L -> {
                                //konversi nilai ke Watt (W)
                                binding.hasil.setText((angkaDesimal(dayaPump)))
                                dayaPumpSave = "${angkaDesimal(dayaPump)} W"
                            }
                            1L -> {
                                //konversi nilai ke kiloWatt (kW)
                                binding.hasil.setText((angkaDesimal((dayaPump / 1000))))
                                dayaPumpSave = "${angkaDesimal(dayaPump)} kW"
                            }
                            2L -> {
                                //konversi nilai ke HorsePower (HP)
                                binding.hasil.setText((angkaDesimal((dayaPump / 746))))
                                dayaPumpSave = "${angkaDesimal(dayaPump)} HP"
                            }
                        }
                        btnSimpanVisible()
                    }
                }
            }
        }

        //menentukan jenis satuan yang digunakan
        binding.spSatuan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (binding.spSatuan.selectedItemId) {
                    0L -> {
                        //konversi nilai ke Watt (W)
                        binding.hasil.setText((angkaDesimal(dayaPump)))
                        dayaPumpSave = "${angkaDesimal(dayaPump)} W"
                    }
                    1L -> {
                        //konversi nilai ke kiloWatt (kW)
                        binding.hasil.setText((angkaDesimal((dayaPump / 1000))))
                        dayaPumpSave = "${angkaDesimal(dayaPump / 1000)} kW"
                    }
                    2L -> {
                        //konversi nilai ke HorsePower (HP)
                        binding.hasil.setText((angkaDesimal((dayaPump / 746))))
                        dayaPumpSave = "${angkaDesimal(dayaPump / 746)} HP"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        with(binding) {
            diameterPipa.afterTextChanged()
            debitAliran.afterTextChanged()
            ketinggian1.afterTextChanged()
            ketinggian2.afterTextChanged()
            panjangEkivalenPipa.afterTextChanged()
            efisiensi.afterTextChanged()
            keranGerbang.afterTextChanged()
            keranBola.afterTextChanged()
            keranSudut.afterTextChanged()
            tekanan1.afterTextChanged()
            tekanan2.afterTextChanged()
            kecepatan1.afterTextChanged()
            kecepatan2.afterTextChanged()
            kerapatan.afterTextChanged()
            viskositas.afterTextChanged()
            belokan45.afterTextChanged()
            belokan90.afterTextChanged()
            diameterPipaBaru.afterTextChanged()
        }
    }

    /**
     * berfungsi untuk mengatur jumlah angka dibelakang koma dan mengkonversi tipe data ke string
     */
    private fun angkaDesimal(angka: Double): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(angka)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    //Membuat pilihan menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_list_data -> {
                val i = Intent(this, ListDataActivity::class.java)
                startActivity(i)
            }
            R.id.btn_info -> {
                val i = Intent(this, InfoActivity::class.java)
                startActivity(i)
            }
        }
        return true
    }

    private fun btnSimpanGone() {
        binding.btnSimpan.visibility = View.GONE
    }

    private fun btnSimpanVisible() {
        binding.btnSimpan.visibility = View.VISIBLE
    }

    /**
     * mengambil data tanggal dari SimmpleDateFormat
     * melakukan penyimpanan data ke database room menggunakan coroutine
     */
    private suspend fun saveData(name: String, dataEt0String: String) {
        val sf = SimpleDateFormat("dd MM yyyy", Locale("ID")).format(Date())
        val data = DataEntity(0, name, sf, dataEt0String)

        db.dataDao().insertData(data)
    }

    private fun showSaveDialog() {
        dialog.show()
    }

    /**
     * menampilkan dialog konfirmasi penyimpnan data ke database dan meminta user menginput nama data
     */
    private fun initiationDialogSave() {
        dialog = Dialog(this)
        //Mengeset judul dialog
        dialog.setTitle("Simpan Data")

        //Mengeset layout
        dialog.setContentView(bindingTemplate.root )

        //Membuat agar dialog tidak hilang saat di click di area luar dialog
        dialog.setCanceledOnTouchOutside(false)

        //Membuat dialog agar berukuran responsive
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        dialog.window?.setLayout(6 * width / 7, LinearLayout.LayoutParams.WRAP_CONTENT)

        bindingTemplate.btnSave.setOnClickListener {
            val title = bindingTemplate.tvTitle.text.toString()
            CoroutineScope(Dispatchers.IO).launch {
                saveData(title, dayaPumpSave)
            }
            bindingTemplate.tvTitle.setText("")
            dialog.dismiss()
            Toast.makeText(this@MainActivity, "Data saved", Toast.LENGTH_SHORT).show()
        }
        bindingTemplate.btnCancel.setOnClickListener { dialog.dismiss() }

    }

    /**
     * extension untuk membuat button simpan hilang ketika terjadi perubahan pada edittext
     */
    private fun EditText.afterTextChanged() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSimpanGone()
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setMessage("Yakin Ingin Keluar?")
        builder.setPositiveButton("Ya") { _, _ ->
            finish()
        }
        builder.setNegativeButton(
                "Tidak"
        ) { dialog, _ ->
            dialog.cancel()
        }
        val alert = builder.create()
        alert.show()
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }
}