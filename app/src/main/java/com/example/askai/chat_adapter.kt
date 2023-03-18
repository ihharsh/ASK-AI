package com.example.askai

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askai.databinding.ItemViewChatviewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class chat_adapter(messageList: ArrayList<Message>): RecyclerView.Adapter<chat_adapter.myViewHolder>() {

    inner class myViewHolder(val binding: ItemViewChatviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    var messageList = messageList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        return myViewHolder(
            ItemViewChatviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {

        var message: Message = messageList[position]

        if (message.sentBy == Message.SENT_BY_ME) {
            setRightChat(holder, message)
        } else if (message.sentBy == Message.SENT_BY_AI) {
            setLeftChatForText(holder, message)
        } else if (message.sentBy == Message.SENT_BY_AI_IMAGE) {
            setLeftChatForImage(holder, message)
        }


    }


    private fun setLeftChatForImage(holder: myViewHolder, message: Message) {
        holder.binding.llLeftChat.visibility = View.GONE
        holder.binding.llRightChat.visibility = View.GONE
        holder.binding.llImageView.visibility = View.VISIBLE
        var url = message.message
        var imageView: ImageView = holder.binding.imageView
        Glide.with(imageView.context).load(url).into(imageView)


        holder.binding.llImageView.setOnLongClickListener {

            ImageMenu(it, holder)
            true
        }

    }

    private fun setLeftChatForText(holder: myViewHolder, message: Message) {
        holder.binding.llRightChat.visibility = View.GONE
        holder.binding.llImageView.visibility = View.GONE
        holder.binding.llLeftChat.visibility = View.VISIBLE
        holder.binding.tvLeftChat.text = (message.message)

        holder.binding.llLeftChat.setOnLongClickListener {
            var text = holder.binding.tvLeftChat.text.toString()
            CopyTextMenu(it, text)
            true
        }

    }

    private fun setRightChat(holder: myViewHolder, message: Message) {
        holder.binding.llLeftChat.visibility = View.GONE
        holder.binding.llImageView.visibility = View.GONE
        holder.binding.llRightChat.visibility = View.VISIBLE
        holder.binding.tvRightChat.text = message.message

        holder.binding.tvRightChat.setOnLongClickListener {
            var text = holder.binding.tvRightChat.text.toString()
            CopyTextMenu(it, text);
            true
        }
    }

    private fun CopyTextMenu(view: View, message: String) {

        val menu = PopupMenu(view.context, view)
        menu.inflate(R.menu.copy_menu)

        menu.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.copy -> {
                    val clipboard =
                        view.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("", message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(view.context, "Text Copied", Toast.LENGTH_SHORT).show()

                }
            }
            true
        }
        menu.show()

        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menuPopup = popup.get(menu)
        menuPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menuPopup, true)


    }

    private fun ImageMenu(view: View, holder: myViewHolder) {

        val menu = PopupMenu(view.context, view)
        menu.inflate(R.menu.share_download_image_menu)

        menu.setOnMenuItemClickListener {
            val bitmapDrawable = holder.binding.imageView.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            when (it.itemId) {
                R.id.download -> {
                    // download Image code



                    saveMediaToStorage(bitmap,view.context)





                }

                R.id.share -> {
                    // share Image code

                   share(bitmap,view.context)
                }
            }
            true
        }
        menu.show()

        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menuPopup = popup.get(menu)
        menuPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menuPopup, true)


    }

    fun saveMediaToStorage(bitmap: Bitmap, context: Context) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context?.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show()
        }
    }

    fun share(bitmap: Bitmap, context: Context) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        var imageUri : Uri? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context?.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                 imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show()
        }

        imageUri?.let { shareImage(it,context) }


    }

    private fun shareImage(uri: Uri,context: Context){

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/jpeg"
        context.startActivity(Intent.createChooser(shareIntent, "Share"))
    }







}