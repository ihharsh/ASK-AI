package com.example.askai

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.askai.databinding.ActivityMainBinding
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var messageList: ArrayList<Message>
    lateinit var message_adapter: chat_adapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

          messageList = ArrayList<Message>()


         message_adapter = chat_adapter(messageList)

        binding.recyclerView.adapter = message_adapter

        var linearlayout: LinearLayoutManager = LinearLayoutManager(this)
        linearlayout.stackFromEnd = true

        binding.recyclerView.layoutManager = linearlayout



        binding.sendBtn.setOnClickListener {
            var question = binding.editTextMessage.text.toString().trim()
            onClickSendButton(question)


        }

    }

    fun onClickSendButton(question: String) {

        binding.tvWelcomeText.visibility = View.GONE
        if(question.startsWith('/')){
            addtoChat(question,Message.SENT_BY_ME)
            binding.editTextMessage.setText("")

            addtoChat("Generating Image...",Message.SENT_BY_AI)
            // generate image

            getResponseImage(question)


        } else {
            addtoChat(question,Message.SENT_BY_ME)
            binding.editTextMessage.setText("")
            addtoChat("Typing...",Message.SENT_BY_AI)
            getResponseVolley(question)
        }



    }

    private fun addImagetoChat(url: String, sentByAiImage: String) {
        messageList.removeAt(messageList.size-1)
        runOnUiThread {
            messageList.add(Message(url, sentByAiImage))
            message_adapter.notifyDataSetChanged()
            binding.recyclerView.smoothScrollToPosition(message_adapter.itemCount)
        }
    }

    fun addtoChat(message:String, sentBy:String){

        runOnUiThread {
            messageList.add(Message(message, sentBy))
            message_adapter.notifyDataSetChanged()
            binding.recyclerView.smoothScrollToPosition(message_adapter.itemCount)
        }


    }

    fun addResponse(response: String){
        messageList.removeAt(messageList.size - 1)
        addtoChat(response,Message.SENT_BY_AI)
    }

    private fun getResponseVolley(query: String) {

        val url = "https://api.openai.com/v1/completions"

        // creating a queue for request queue.
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        // creating a json object on below line.
        val jsonObject: JSONObject? = JSONObject()
        // adding params to json object.
        jsonObject?.put("model", "text-davinci-003")
        jsonObject?.put("prompt", query)
        jsonObject?.put("temperature", 0)
        jsonObject?.put("max_tokens", 100)
        jsonObject?.put("top_p", 1)
        jsonObject?.put("frequency_penalty", 0.0)
        jsonObject?.put("presence_penalty", 0.0)

        // on below line making json object request.
        val postRequest: JsonObjectRequest =
            // on below line making json object request.
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    // on below line getting response message and setting it to text view.
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")
                   // addtochat

                    addResponse(responseMsg.trim())
                },
                // adding on error listener
                Response.ErrorListener { error ->
                    messageList.removeAt(messageList.size - 1)
                    addtoChat("ERROR!",Message.SENT_BY_AI)
                    Log.e("TAGAPI", "Error is : " + error.message + "\n" + error)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    // adding headers on below line.
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer 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"
                    return params;
                }
            }


        // on below line adding our request to queue.
        queue.add(postRequest)
    }

    private fun getResponseImage(prompt: String) {

        // binding.progressCircular.visibility = View.VISIBLE
        val url_og = "https://api.openai.com/v1/images/generations"
        val queue = Volley.newRequestQueue(this)





        val jsonObject: JSONObject? = JSONObject()

        jsonObject?.put("prompt",prompt)
        jsonObject?.put("n",1)
        jsonObject?.put("size","256x256")




        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST, url_og, jsonObject,
            Response.Listener { response ->



                val currentImageUrl: String = response.getJSONArray("data").getJSONObject(0).getString("url")

                addImagetoChat(currentImageUrl,Message.SENT_BY_AI_IMAGE);


            },
            Response.ErrorListener { error ->
                messageList.removeAt(messageList.size-1)
                addtoChat("ERROR!",Message.SENT_BY_AI)

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {

                val params: MutableMap<String, String> = HashMap()
                // adding headers on below line.
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer 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"
                return params;

            }
        }

        queue.add(jsonObjectRequest)



    }

}