/**
 * @author Ahlillah
 * @since 1.0
 */

package id.tpusk.headpump

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import id.tpusk.headpump.databinding.ActivityMainBinding
import id.tpusk.headpump.databinding.DialogTamplateBinding
import id.tpusk.headpump.room.DataDB
import id.tpusk.headpump.room.DataEntity
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
            val g = 9.81

            /**
             * area inputan
             */
            with(binding) {

                /**
                 * validasi data
                 */
                when {
                    diameterPipa.text?.isBlank() == true -> diameterPipa.error = getString(R.string.diameter_error)
                    debitAliran.text?.isBlank() == true -> debitAliran.error = getString(R.string.debit_error)
                    ketinggian1.text?.isBlank() == true -> ketinggian1.error = getString(R.string.ketinggian1_error)
                    ketinggian2.text?.isBlank() == true -> ketinggian2.error = getString(R.string.ketinggian2_error)
                    panjangEkivalenPipa.text?.isBlank() == true -> panjangEkivalenPipa.error = getString(R.string.panjang_ekivalen_error)
                    efisiensi.text?.isBlank() == true -> efisiensi.error = getString(R.string.efisiensi_error)
                    keranGerbang.text?.isBlank() == true -> keranGerbang.error = getString(R.string.keran_gerbang_error)
                    keranBola.text?.isBlank() == true -> keranBola.error = getString(R.string.keran_bola_error)
                    keranSudut.text?.isBlank() == true -> keranSudut.error = getString(R.string.keran_sudut_error)
                    tekanan1.text?.isBlank() == true -> tekanan1.error = getString(R.string.tekanan1_error)
                    tekanan2.text?.isBlank() == true -> tekanan2.error = getString(R.string.tekanan2_error)
                    kecepatan1.text?.isBlank() == true -> kecepatan1.error = getString(R.string.kecepatan1_error)
                    kecepatan2.text?.isBlank() == true -> kecepatan2.error = getString(R.string.kecepatan2_error)
                    kerapatan.text?.isBlank() == true -> kerapatan.error = getString(R.string.kerapatan_error)
                    viskositas.text?.isBlank() == true -> viskositas.error = getString(R.string.viskositas_error)
                    belokan45.text?.isBlank() == true -> belokan45.error = getString(R.string.belokan45_error)
                    belokan90.text?.isBlank() == true -> belokan90.error = getString(R.string.belokan90_error)
                    diameterPipaBaru.text?.isBlank() == true -> diameterPipaBaru.error = getString(R.string.diameter_baru_error)

                    /**
                     * inisisasi data
                     * konversi mm ke m
                     * konversi jam ke detik
                     * mengubah diameter ke jari jari
                     * menetukan luas penampang pipa
                     * menentukan kecepatan aliran
                     * menentukan nilai reynold
                     * menentukan jenis aliran
                     */

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

                        val f: Double

                        val diameterPipa = diameterPipamm / 1000
                        val debitAliran = debitAliranh / 3600
                        val r = diameterPipa / 2
                        val a = (3.14 * r.pow(2))
                        val v = (debitAliran / a)
                        val re = (kerapatan * v * diameterPipa) / viskositas

                        /**
                         * re > 2300 == LAMINER
                         * re in 2300..4000 == TRANSITION
                         * re > 4000 == TURBULEN
                         */

                        //friction
                        f = if (re < 2300) 64 / re else (0.3164 / re.pow(0.25)) //menentukan nilai koefisien gesekan

                        //Entrance Length
                        val le = if (re < 2300) 138 * diameterPipa else 35 * diameterPipa //menentukan nilai length entrance

                        //headloss keran gerbang
                        val ldg = 8 //konstanta rasio panjang pipa dengan diameter pipa untuk keran gerbang
                        val headlossg = f * ldg * v.pow(2) * keranGerbang / (2 * g) //menentukan nilai headloss keran gerbang

                        //headloss keran bola
                        val ldb = 340 //konstanta rasio panjang pipa dengan diameter pipa untuk keran bola
                        val headlossb = f * ldb * v.pow(2) * keranBola / (2 * g) //menentukan nilai headloss keran bola

                        //headloss keran sudut
                        val lds = 150 //konstanta rasio panjang pipa dengan diameter pipa untuk keran sudut
                        val headlosss = f * lds * v.pow(2) * keranSudut / (2 * g) //menentukan nilai headloss keran sudut

                        //Headloss belokan 90 deg
                        val ld90 = 30 //konstanta rasio panjang pipa dengan diameter pipa untuk belokan 90 derajat
                        val headloss90 = (f * ld90 * v.pow(2) * belokan90) / (2 * g) //menentukan nilai headloss belokan 90 derajat

                        //Headloss belokan 45 deg
                        val ld45 = 16 //konstanta rasio panjang pipa dengan diameter pipa untuk belokan 45 derajat
                        val headloss45 =
                                (f * ld45 * v.pow(2) * belokan45) / (2 * g) //menentukan nilai headloss belokan 45 derajat

                        //Perbandingan diameter
                        val diameterPipaBaru = diameterPipaBarumm / 1000 //konversi mm ke m
                        val perbandinganDiameter =
                                if (diameterPipaBaru <= diameterPipa) diameterPipaBaru.pow(2) / diameterPipa.pow(2) else diameterPipa.pow(2) / diameterPipaBaru.pow(
                                        2
                                ) //menentukan nilai perbandingan perubahan diameter

                        //koefisient diameter
                        val koefisientDiameter =
                                if (perbandinganDiameter <= 0.715) 0.4 * (1.25 - perbandinganDiameter) else 0.75 * (1 - perbandinganDiameter) //menentukan nilai koefisien headloss diameter

                        //headloss mayor
                        val headlossMayor =
                                f * panjangEkivalenPipa * v.pow(2) / (2 * g * diameterPipa) //menentukan nilai headloss mayor

                        //headloss entrace
                        val ken = 0.5 //koefisient untuk headloss entrance
                        val headlosse = ken * v.pow(2) / (2 * g) //menenutkan nilai headloss entrance

                        //headloss keran total
                        val headlosskt = headlossg + headlossb + headlosss //menentukan nilai headloss keran total

                        //headloss belokan
                        val headlossBel = headloss45 + headloss90 //menentukan nilai headloss belokan total

                        //headloss diameter
                        val headlossd = koefisientDiameter * v.pow(2) / (2 * g * diameterPipa) //menenutkan nilai headloss diameter

                        //headloss minor total
                        val headlossMt: Double = headlosse + headlosskt + headlossBel //menentukan nilai headloss minor total

                        //headloss total sistem
                        val headlossTotal = headlossMayor + headlossMt //menentukan nilai headloss total pada sistem

                        //head pump total sistem
                        val headPump =
                                (ketinggian2 + (tekanan2 / (kerapatan * g)) + (kecepatanAliran2.pow(2) / (2 * g))) - (ketinggian1 + (tekanan1 / (kerapatan * g)) + (kecepatanAliran1.pow(
                                        2
                                ) / (2 * g))) + headlossTotal //menentukan nilai headloss total pafa sistem pompa

                        //daya pump total
                        dayaPump = kerapatan * g * debitAliran * headPump * (efisiensi / 100) //menentukan nilai daya pompa yang dibuthkan

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
     * mengambil data tanggal dari SimpleDateFormat
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
            Toast.makeText(this@MainActivity, getString(R.string.pesan_data_tersimpan), Toast.LENGTH_SHORT).show()
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
        builder.setMessage(getString(R.string.pesan_konfirmasi_keluar_aplikasi))
        builder.setPositiveButton(getString(R.string.ya)) { _, _ ->
            finish()
        }
        builder.setNegativeButton(
                getString(R.string.tidak)
        ) { dialog, _ ->
            dialog.cancel()
        }
        val alert = builder.create()
        alert.show()
    }

}