package com.devanasmohammed.locationreminder.locationreminders.reminderslist

import com.devanasmohammed.locationreminder.R
import com.devanasmohammed.locationreminder.base.BaseRecyclerViewAdapter

//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}