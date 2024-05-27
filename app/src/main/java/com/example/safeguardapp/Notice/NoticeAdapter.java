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
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_notice,parent,false);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeAdapter.NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        String title = notice.getTitle();
        String content = notice.getContent();
        String date = notice.getDate();
        String type = notice.getType();
        String child = notice.getChild();

        holder.textView.setText(title+content+date+type+child);
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder{
        TextView textView;

        public NoticeViewHolder(@NonNull View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
        }
    }
}
