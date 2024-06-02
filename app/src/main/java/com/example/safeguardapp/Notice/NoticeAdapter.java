package com.example.safeguardapp.Notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safeguardapp.R;
import com.example.safeguardapp.data.model.Notice;

import java.util.ArrayList;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter <NoticeAdapter.NoticeViewHolder> {
    private List<Notice> noticeList;
    private Context context;

    public NoticeAdapter(Context context, ArrayList<Notice> noticeList){
        this.context = context;
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeAdapter.NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notice_textview,parent,false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeAdapter.NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);

        String title = notice.getTitle();
        String content = notice.getContent();
        String date = notice.getDate();
//        String child = notice.getChild(); -> notice_textview 에서 사용 안함.
        String senderId = notice.getSenderId();

//        content = content.split(": ")[1];
//        String[] newDate = date.split("T");
//        String[] time = newDate[1].split(".");
//        date = newDate[0]+time[1];

        holder.textTitle.setText(title);
        holder.textContent.setText(content);
        holder.textDate.setText(date);
        holder.textSenderId.setText("발신자 : " + senderId);
//        holder.textChild.setText(child);

    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder{
        TextView textTitle;
        TextView textContent;
        TextView textDate;
        TextView textSenderId;
//        TextView textChild;

        public NoticeViewHolder(@NonNull View itemView){
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textContent = itemView.findViewById(R.id.text_content);
            textDate = itemView.findViewById(R.id.text_date);
            textSenderId = itemView.findViewById(R.id.text_SenderId);
//            textChild = itemView.findViewById(R.id.text_child);

        }
    }
}
