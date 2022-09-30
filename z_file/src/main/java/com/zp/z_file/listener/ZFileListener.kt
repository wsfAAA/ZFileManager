package com.zp.z_file.listener

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.zp.z_file.R
import com.zp.z_file.common.ZFileType
import com.zp.z_file.content.*
import com.zp.z_file.type.*
import com.zp.z_file.ui.ZFileListFragment
import com.zp.z_file.ui.ZFilePicActivity
import com.zp.z_file.ui.ZFileVideoPlayActivity
import com.zp.z_file.ui.dialog.*
import com.zp.z_file.util.ZFileHelp
import com.zp.z_file.util.ZFileLog
import com.zp.z_file.util.ZFileOpenUtil
import com.zp.z_file.util.ZFileUtil
import java.io.File

/*
本库内置丰富的api、内置丰富的配置属性，足以胜任开发者的个性化需求！
极高自定义（文件获取、文件操作、文件类型扩展、UI展示、主题、提示语句等），简单配置即可满足需求
 */

/**
 * 图片或视频 显示
 */
abstract class ZFileImageListener {

    /**
     * 图片类型加载
     */
    abstract fun loadImage(imageView: ImageView, file: File)

    /**
     * 视频类型加载
     */
    open fun loadVideo(imageView: ImageView, file: File) {
        loadImage(imageView, file)
    }
}

/**
 * 文件选取 后 的监听
 */
interface ZFileSelectResultListener {

    fun selectResult(selectList: MutableList<ZFileBean>?)

}

/**
 * 完全自定义 获取文件数据
 */
interface ZFileLoadListener {

    /**
     * 获取手机里的文件List
     * @param filePath String           指定的文件目录访问，空为SD卡根目录
     * @return MutableList<ZFileBean>?  list
     */
    fun getFileList(context: Context?, filePath: String?): MutableList<ZFileBean>?
}

/**
 * 嵌套在 Fragment 中 使用
 * [FragmentActivity] 中 对于 [ZFileListFragment] 操作
 */
abstract class ZFragmentListener {

    /**
     * 文件选择
     */
    abstract fun selectResult(selectList: MutableList<ZFileBean>?)

    /**
     * [Activity] 中直接调用 [Activity.finish] 即可，如有需要，重写即可
     */
    open fun onActivityBackPressed(activity: FragmentActivity) {
        activity.finish()
    }

    /**
     * 获取 [Manifest.permission.WRITE_EXTERNAL_STORAGE] 权限失败
     * @param activity [FragmentActivity]
     */
    open fun onSDPermissionsFiled(activity: FragmentActivity) {
        activity.toast(activity getStringById R.string.zfile_permission_bad)
    }

    /**
     * 获取 [Environment.isExternalStorageManager] (所有的文件管理) 权限 失败
     * 请注意：Android 11 及以上版本 才有
     */
    open fun onExternalStorageManagerFiled(activity: FragmentActivity) {
        activity.toast(activity getStringById R.string.zfile_11_bad)
    }
}

/**
 * 完全自定义 QQ、WeChat 获取
 */
abstract class ZQWFileLoadListener {

    /**
     * 获取标题
     * @return Array<String>
     */
    open fun getTitles(): Array<String>? = null

    /**
     * 获取过滤规则
     * @param fileType Int      文件类型 see [ZFILE_QW_PIC]、[ZFILE_QW_MEDIA]、[ZFILE_QW_DOCUMENT]、[ZFILE_QW_OTHER]
     */
    abstract fun getFilterArray(fileType: Int): Array<String>

    /**
     * 获取 QQ 或 WeChat 文件路径
     * @param qwType String         QQ 或 WeChat  see [ZFileConfiguration.QQ]、[ZFileConfiguration.WECHAT]
     * @param fileType Int          文件类型 see [ZFILE_QW_PIC]、[ZFILE_QW_MEDIA]、[ZFILE_QW_DOCUMENT]、[ZFILE_QW_OTHER]
     * @return MutableList<String>  文件路径集合（因为QQ或WeChat保存的文件可能存在多个路径）
     */
    abstract fun getQWFilePathArray(qwType: String, fileType: Int): MutableList<String>

    /**
     * 获取数据
     * @param fileType Int                          文件类型 see [ZFILE_QW_PIC]、[ZFILE_QW_MEDIA]、[ZFILE_QW_DOCUMENT]、[ZFILE_QW_OTHER]
     * @param qwFilePathArray MutableList<String>   QQ 或 WeChat 文件路径集合
     * @param filterArray Array<String>             过滤规则
     */
    abstract fun getQWFileDatas(fileType: Int, qwFilePathArray: MutableList<String>, filterArray: Array<String>): MutableList<ZFileBean>

}

/**
 * 文件类型
 */
open class ZFileTypeListener {

    open fun getFileType(filePath: String): ZFileType {
        return when (ZFileHelp.getFileTypeBySuffix(filePath)) {
            PNG, JPG, JPEG, GIF -> ImageType()
            MP3, AAC, WAV, M4A -> AudioType()
            MP4, _3GP -> VideoType()
            TXT, XML, JSON -> TxtType()
            ZIP -> ZipType()
            DOC, DOCX -> WordType()
            XLS, XLSX -> XlsType()
            PPT, PPTX -> PptType()
            PDF -> PdfType()
            else -> OtherType()
        }
    }
}

/**
 * 打开文件
 */
open class ZFileOpenListener {

    /**
     * 打开音频
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openAudio(filePath: String, view: View) {
        (view.context as? AppCompatActivity)?.apply {
            val tag = "ZFileAudioPlayDialog"
            checkFragmentByTag(tag)
            ZFileAudioPlayDialog.getInstance(filePath).show(supportFragmentManager, tag)
        }
    }

    /**
     * 打开图片
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openImage(filePath: String, view: View) {
        view.context?.let {
            ZFilePicActivity.show(it, filePath)
        }
    }

    /**
     * 打开视频
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openVideo(filePath: String, view: View) {
        view.context?.let {
            ZFileVideoPlayActivity.show(it, filePath)
        }
    }

    /**
     * 打开Txt
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openTXT(filePath: String, view: View) {
        ZFileOpenUtil.openTXT(filePath, view)
    }

    /**
     * 打开zip
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openZIP(filePath: String, view: View) {
        view.context?.let {
            AlertDialog.Builder(it).apply {
                setTitle("请选择")
                setItems(arrayOf("打开", "解压")) { dialog, which ->
                    if (which == 0) {
                        ZFileOpenUtil.openZIP(filePath, view)
                    } else {
                        zipSelect(filePath, it)
                    }
                    dialog.dismiss()
                }
                setPositiveButton("取消") { dialog, _ -> dialog.dismiss() }
                show()
            }
        }
    }

    private fun zipSelect(filePath: String, context: Context) {
        if (context is AppCompatActivity) {
            context.checkFragmentByTag("ZFileSelectFolderDialog")
            val dialog = ZFileSelectFolderDialog.newInstance("解压")
            dialog.selectFolder = {
                getZFileHelp().getFileOperateListener().zipFile(filePath, this, context) {
                    ZFileLog.i(if (this) "解压成功" else "解压失败")
                    val fragment =
                        context.supportFragmentManager.findFragmentByTag(getZFileConfig().fragmentTag)
                    if (fragment is ZFileListFragment) {
                        fragment.observer(this)
                    } else {
                        ZFileLog.e("文件解压成功，但是无法立刻刷新界面！")
                    }
                }
            }
            dialog.show(context.supportFragmentManager, "ZFileSelectFolderDialog")
        } else {
            ZFileLog.e("文件解压 showDialog 失败")
        }
    }

    /**
     * 打开word
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openDOC(filePath: String, view: View) {
        ZFileOpenUtil.openDOC(filePath, view)
    }

    /**
     * 打开xls
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openXLS(filePath: String, view: View) {
        ZFileOpenUtil.openXLS(filePath, view)
    }

    /**
     * 打开PPT
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openPPT(filePath: String, view: View) {
        ZFileOpenUtil.openPPT(filePath, view)
    }

    /**
     * 打开PDF
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openPDF(filePath: String, view: View) {
        ZFileOpenUtil.openPDF(filePath, view)
    }

    /**
     * 打开其他文件类型
     * @param filePath String   文件路径
     * @param view View         RecyclerView itemView
     */
    open fun openOther(filePath: String, view: View) {
        ZFileLog.e("【${filePath.getFileType()}】不支持预览该文件 ---> $filePath")
        view.toast("暂不支持预览该文件")
    }
}

/**
 * 文件操作（默认不支持对于文件夹的操作，如果需要对于文件夹的操作，请重写该类的所有方法）！
 * 耗时的文件操作建议放在 非 UI线程中
 */
open class ZFileOperateListener {

    /**
     * 文件重命名（该方式需要先弹出重命名弹窗或其他页面，再执行重命名逻辑）
     * @param filePath String   文件路径
     * @param context Context   Context
     * @param block Function2<Boolean, String, Unit> Boolean：成功或失败；String：新名字
     */
    open fun renameFile(
        filePath: String,
        context: Context,
        block: (Boolean, String) -> Unit
    ) {
        (context as? AppCompatActivity)?.let {
            it.checkFragmentByTag("ZFileRenameDialog")
            ZFileRenameDialog.newInstance(filePath.getFileNameOnly()).apply {
                reanameDown = {
                    renameFile(filePath, this, context, block)
                }
            }.show(it.supportFragmentManager, "ZFileRenameDialog")
        }
    }

    /**
     * 文件重命名（该方式只需要实现重命名逻辑即可）
     * @param filePath String       文件路径
     * @param fileNewName String    新名字
     * @param context Context       Context
     * @param block Function2<Boolean, String, Unit> Boolean：成功或失败；String：新名字
     */
    open fun renameFile(
        filePath: String,
        fileNewName: String,
        context: Context,
        block: (Boolean, String) -> Unit
    ) {
        ZFileUtil.renameFile(filePath, fileNewName, context, block)
    }

    /**
     * 复制文件
     * @param sourceFile String     源文件地址
     * @param targetFile String     目标文件地址
     * @param context Context       Context
     */
    open fun copyFile(
        sourceFile: String,
        targetFile: String,
        context: Context,
        block: Boolean.() -> Unit
    ) {
        ZFileUtil.copyFile(sourceFile, targetFile, context, block)
    }

    /**
     * 移动文件
     * @param sourceFile String     源文件地址
     * @param targetFile String     目标文件地址
     * @param context Context       Context
     */
    open fun moveFile(
        sourceFile: String,
        targetFile: String,
        context: Context,
        block: Boolean.() -> Unit
    ) {
        ZFileUtil.cutFile(sourceFile, targetFile, context, block)
    }

    /**
     * 删除文件
     * @param filePath String   源文件地址
     */
    open fun deleteFile(filePath: String, context: Context, block: Boolean.() -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle("温馨提示")
            setMessage("您确定要删除吗？")
            setPositiveButton("删除") { _, _ ->
                ZFileUtil.deleteFile(filePath, context, block)
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    /**
     * 解压文件
     * @param sourceFile String     源文件地址
     * @param targetFile String     目标文件地址
     */
    open fun zipFile(
        sourceFile: String,
        targetFile: String,
        context: Context,
        block: Boolean.() -> Unit
    ) {
        ZFileUtil.zipFile(sourceFile, targetFile, context, block)
    }

    /**
     * 文件详情
     */
    open fun fileInfo(bean: ZFileBean, context: Context) {
        val tag = ZFileInfoDialog::class.java.simpleName
        (context as? AppCompatActivity)?.let {
            it.checkFragmentByTag(tag)
            ZFileInfoDialog.newInstance(bean).show(it.supportFragmentManager, tag)
        }

    }
}

/**
 * 其他操作相关
 */
open class ZFileOtherListener {

    /**
     * 耗时的文件操作（如复制、移动文件等） 展示的 Dialog
     * @param context Context   Context
     * @param title String?     标题
     */
    open fun getLoadingDialog(
        context: Context,
        title: String? = context getStringById R.string.zfile_loading
    ): Dialog {
        return ZFileLoadingDialog(context, title)
    }

    /**
     * 获取 权限失败 时的 布局
     * 请注意：布局中必须包含控件 id：zfile_permission_againBtn
     * 该id对应视图功能：用户点击后再次申请权限
     */
    open fun getPermissionFailedLayoutId(): Int {
        return ZFILE_DEFAULT
    }

    /**
     * 获取 当前目录没有文件时（为空） 的布局
     */
    open fun getFileListEmptyLayoutId(): Int {
        return ZFILE_DEFAULT
    }

}
