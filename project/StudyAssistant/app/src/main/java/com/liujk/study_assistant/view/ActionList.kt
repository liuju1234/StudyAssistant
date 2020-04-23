package com.liujk.study_assistant.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.liujk.study_assistant.R
import com.liujk.study_assistant.action.MyAction

class ActionList(var context: Context, var actions: List<MyAction>) {
    fun display(noteText: String) {
        val actionsLayout = LayoutInflater.from(context).inflate(R.layout.action_list, null)
        val noteView: TextView = actionsLayout.findViewById(R.id.action_note)
        if (noteText == "") {
            noteView.visibility = View.GONE
        } else {
            noteView.visibility = View.VISIBLE
            noteView.text = noteText
        }
        val listView: ListView = actionsLayout.findViewById(R.id.list_view)
        listView.adapter = ActionListAdapter(context, actions)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            .setView(actionsLayout)
        builder.create().show()
    }
}

class ActionListAdapter(var context: Context, var actions: List<MyAction>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_action, null)
        val action = actions[position]
        val itemType = itemView.findViewById<TextView>(R.id.type)
        val itemContent = itemView.findViewById<TextView>(R.id.content)
        itemType.setText(action.type.toString())
        itemContent.setText(action.display)
        itemView.setOnClickListener(View.OnClickListener { action.run(context) })
        return itemView
    }

    override fun getItem(position: Int): Any {
        return actions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return actions.size
    }

}

