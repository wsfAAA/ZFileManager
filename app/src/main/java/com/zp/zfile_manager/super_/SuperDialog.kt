package com.zp.zfile_manager.super_

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zp.z_file.content.ZFileBean
import com.zp.zfile_manager.databinding.DialogSuperBinding

/**
 * 数据已经获取到了，具体怎么操作就交给你了！
 */
class SuperDialog : DialogFragment() {

    private var vb: DialogSuperBinding? = null

    companion object {
        fun newInstance(list: ArrayList<ZFileBean>) = SuperDialog().apply {
            arguments = Bundle().run {
                putParcelableArrayList("list", list)
                this
            }
        }
    }

    private var superAdapter: SuperAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vb = DialogSuperBinding.inflate(inflater, container, false)
        return vb?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        Dialog(context!!, com.zp.z_file.R.style.Zfile_Select_Folder_Dialog).apply {
            window?.setGravity(Gravity.BOTTOM)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("ZFileManager", "数据已经获取到了，具体怎么操作就交给你了！")
        vb?.superDownPic?.setOnClickListener {
            dismiss()
        }
        vb?.superCacelPic?.setOnClickListener {
            dismiss()
        }
        val list = arguments?.getParcelableArrayList<ZFileBean>("list") as ArrayList<ZFileBean>
        superAdapter = SuperAdapter(list)
        vb?.superRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = superAdapter
        }
    }

    override fun onStart() {
        val display = context!!.getTDisplay()
        dialog?.window?.setLayout(display[0], display[1])
        super.onStart()
    }

    override fun onDestroyView() {
        vb = null
        super.onDestroyView()
    }

    private fun Context.getTDisplay() = IntArray(2).apply {
        val point = Point()
        display?.getRealSize(point)
        this[0] = point.x
        this[1] = point.y
    }

}