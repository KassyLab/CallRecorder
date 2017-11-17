package com.kassylab.callrecorder.adapter;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kassylab.callrecorder.R;

/**
 * {@link RecyclerViewCursorAdapter} that can display an Item.
 */

public class CallsRecyclerViewCursorAdapter
        extends RecyclerViewCursorAdapter<CallsRecyclerViewCursorAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        holder.bind(cursor);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            /*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener)
                        mListener.onContactSelected(itemUri, getAdapterPosition());
                }
            });*/

            mContentView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @SuppressLint("SetTextI18n")
        void bind(Cursor cursor) {
            mContentView.setText("Id : " + cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID)));
        }
    }
}
