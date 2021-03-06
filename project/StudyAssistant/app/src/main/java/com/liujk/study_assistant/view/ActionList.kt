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
import com.liujk.study_assistant.action.ActionType
import com.liujk.study_assistant.action.MyAction

class ActionList(var context: Context, var actions: List<MyAction>,
                 var otherActionsMap: HashMap<String, ArrayList<MyAction>> = hashMapOf()) {
    fun display(noteText: String = "") {
        val actionsLayout = LayoutInflater.from(context).inflate(R.layout.action_list, null)
        val noteAreaView: View = actionsLayout.findViewById(R.id.action_note_area)
        val noteView: TextView = actionsLayout.findViewById(R.id.action_note)
        if (noteText == "") {
            noteAreaView.visibility = View.GONE
        } else {
            noteAreaView.visibility = View.VISIBLE
            noteView.text = noteText
            if (actions.isEmpty()) {
                actionsLayout.findViewById<View>(R.id.divider_line).visibility = View.GONE
            }
        }
        val listView: ListView = actionsLayout.findViewById(R.id.list_view)
        listView.adapter = ActionListAdapter(context, actions, otherActionsMap)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            .setView(actionsLayout)
        builder.create().show()
    }
}

class ActionListAdapter(var context: Context, var actions: List<MyAction>,
                        var otherActionsMap: HashMap<String, ArrayList<MyAction>> = hashMapOf()) : BaseAdapter() {
    private val otherActionsKeys: List<String> = otherActionsMap.keys.distinct()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_action, null)
        val itemType = itemView.findViewById<TextView>(R.id.type)
        val itemContent = itemView.findViewById<TextView>(R.id.content)
        if (position < actions.size) {
            val action = actions[position]
            itemType.text = getTypeStr(action.type)
            itemContent.text = action.display
            itemView.setOnClickListener { action.run(context) }
        } else {
            val tittle = otherActionsKeys[position-actions.size]
            val actions = otherActionsMap[tittle] ?: ArrayList<MyAction>()
            itemType.text = getTypeStr(ActionType.OTHER)
            itemContent.text = tittle
            itemView.setOnClickListener { ActionList(context, actions).display(tittle) }
        }
        return itemView
    }

    private fun getTypeStr(type: ActionType): String {
        return when(type) {
            ActionType.URL -> "网址"
            ActionType.VIDEO -> "视频"
            ActionType.APP -> "应用"
            ActionType.OTHER -> "其它"
            else -> ""
        }
    }

    override fun getItem(position: Int): Any {
        return if (position < actions.size) {
            actions[position]
        } else {
            otherActionsMap[otherActionsKeys[position-actions.size]] ?: ArrayList<MyAction>()
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return actions.size + otherActionsMap.size
    }

}

