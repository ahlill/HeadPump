package id.tpusk.headpumpcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import id.tpusk.headpumpcalculator.databinding.ActivityListDataBinding
import id.tpusk.headpumpcalculator.room.DataDB
import id.tpusk.headpumpcalculator.room.DataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListDataActivity : AppCompatActivity() {

    private val db by lazy { DataDB(this) } //inisiasi variabel database ROOM menggunakan lazy
    private val binding by lazy { ActivityListDataBinding.inflate(layoutInflater) } //inisiasi variabel binding dari layout data_list_activity menggunakan lazy
    lateinit var listDataAdapter: ListDataAdapter //mendeklarasikan variabel adapter dari listAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setAdapter() //memanggil adapter
        loadDataList() //memuat untuk recycler view list adapter
    }

    /**
     * mengambil data dari database menggunakan coroutine
     * memasukkan data yang diambil ke adapter
     * menampilkan pemberitahuan jika data dari database kosong
     */
    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch {
            val data = db.dataDao().getAllData()
            withContext((Dispatchers.Main)) {
                listDataAdapter.setData(data)
                if (data.isNullOrEmpty()) {
                    Toast.makeText(applicationContext, "data kosong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * menginisiasi adapter dengan ListDataAdapter
     * menentukan aksi saat adapter diklik dengan menampilkan dialog konfirmasi untuk menghapus
     * memperbaharui recycler view dengan data baru
     */

    private fun setAdapter() = with(binding) {
        rvListData.layoutManager = LinearLayoutManager(this@ListDataActivity)
        rvListData.setHasFixedSize(true)
        listDataAdapter = ListDataAdapter(arrayListOf())
        rvListData.adapter = listDataAdapter

        listDataAdapter.setOnItemClickCallback(object : ListDataAdapter.OnItemClickCallback {
            override fun onLongClicked(dataList: DataEntity?) {
                showAlertDialogDelete(dataList)
            }

        })
        rvListData.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = listDataAdapter
        }
    }

    /**
     * memuat data dari database menggunakan coroutine
     */
    private fun loadDataList() {
        CoroutineScope(Dispatchers.IO).launch {
            listDataAdapter.setData(db.dataDao().getAllData())
            withContext(Dispatchers.Main) {
                listDataAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * menampilkan dialog konfirmasi hapus data
     */
    private fun showAlertDialogDelete(dataList: DataEntity?) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle(getString(R.string.title_dialog))
            setMessage(getString(R.string.massage_dialog))

            setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.dataDao().deleteData(dataList)
                    dialogInterface.dismiss()
                    loadDataList()
                }
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
        }
        alertDialogBuilder.show()
    }
}