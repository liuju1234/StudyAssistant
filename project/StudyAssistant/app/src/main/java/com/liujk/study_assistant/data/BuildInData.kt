package com.liujk.study_assistant.data;

object BuildInData {

        const val configRawString: String = """
{
    "weekDays": {
        "星期一": {"displayStr": "星期一", "field": "monday", "alias": ["周一", "Mon"]},
        "星期二": {"displayStr": "星期二", "field": "tuesday", "alias": ["周二", "Tus"]},
        "星期三": {"displayStr": "星期三", "field": "wednesday", "alias": ["周三", "Wed"]},
        "星期四": {"displayStr": "星期四", "field": "thursday", "alias": ["周四", "Thu"]},
        "星期五": {"displayStr": "星期五", "field": "friday", "alias": ["周五", "Fri"]},
        "星期六": {"displayStr": "星期六", "field": "saturday", "alias": ["周六", "Sat"]},
        "星期日": {"displayStr": "星期日", "field": "sunday", "alias": ["周日", "Sun"]},
    },
    "processContents": {
        "语文": {"name": "语文", "type": "class", "noMerge": true},
        "语文2": {"name": "语文2", "type": "class", "noMerge": true},
        "数学": {"name": "数学", "type": "class", "noMerge": true},
        "英语": {"name": "英语", "type": "class", "noMerge": true},
        "班会": {"name": "班会", "type": "class"},
        "眼保健操": {"name": "眼保健操", "type": "action"},
        "大课间": {"name": "大课间", "type": "rest"},
        "线上升国旗": {"name": "线上升国旗", "type": "action"},
        "体育": {"name": "体育", "type": "action"},
        "自由梳理": {"name": "自由梳理", "type": "action"},
        "午间休息": {"name": "午间休息", "type": "rest", "info": "/家务劳动"},
        "美术": {"name": "美术", "type": "class"},
        "科学": {"name": "科学", "type": "class", "alias": "科技"},
        "道法": {"name": "道法", "type": "class", "alias": "道德"},
        "音乐": {"name": "音乐", "type": "class"},
        "其它": {"name": "其它", "type": "class"},
    },
    "processesTable": [
        {"time": {"hour":8, "minute":0}, "duration": 25,
            "processes": ["数学"]},
        {"time": {"hour":8, "minute":25}, "duration": 5,
            "processes": ["眼保健操"], "mergeCell": true},
        {"time": {"hour":8, "minute":30}, "duration": 30,
            "processes": ["线上升国旗", "大课间"], "mergeCell": true},
        {"time": {"hour":9, "minute":0}, "duration": 25,
            "processes": ["语文"]},
        {"time": {"hour":9, "minute":25}, "duration": 5,
            "processes": ["眼保健操"], "mergeCell": true},
        {"time": {"hour":9, "minute":30}, "duration": 30,
            "processes": ["体育"], "mergeCell": true},
        {"time": {"hour":10, "minute":0}, "duration": 25,
            "processes": ["英语", "英语", "英语", "英语", "班会", "英语"]},
        {"time": {"hour":10, "minute":25}, "duration": 5,
            "processes": ["眼保健操"], "mergeCell": true},
        {"time": {"hour":10, "minute":30}, "duration": 60,
            "processes": ["语文2","自由梳理","自由梳理","自由梳理","语文2"], "mergeCell": true},
        {"time": {"hour":11, "minute":30}, "duration": 150,
            "processes": ["午间休息"], "mergeCell": true},
        {"time": {"hour":14, "minute":0}, "duration": 90,
            "processes": ["其它", "其它", "道法", "其它", "道法"], "mergeCell": true},
    ],
    "displayDays": [
        {"day": "星期一", "index": 0},
        {"day": "星期二", "index": 1},
        {"day": "星期三", "index": 2},
        {"day": "星期四", "index": 3},
        {"day": "星期五", "index": 4},
    ],
    "notes": {
        "语文": {
            "common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
            }
        },
        "数学": {
            "common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
            }
        },
        "英语": {
            "common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
            }
        },
    },
    "urls": {
        "语文": {
            "common": "https://v.campus.qq.com/gkk/3cumat9#/course",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
            }
        },
        "英语": {
            "common": "",
            "days": {
                "星期一": "",
                "星期二": "",
                "星期三": "",
                "星期四": "",
                "星期五": "",
            }
        },
    },
    "intents": {
        "体教通": {
            "action": "com.tencent.mm.action.WX_SHORTCUT",
            "component": "com.tencent.mm/.plugin.base.stub.WXShortcutEntryActivity",
         },
        "一起小学学生": {
            "package": "com.A17zuoye.mobile.homework",
            "component": "com.A17zuoye.mobile.homework/.main.activity.WelcomeActivity",
         },
    },
    "apps": {
        "体育": {
            "common": "体教通",
        },
        "英语": {
            "common": "一起小学学生",
        },
    },
}
"""

        }
