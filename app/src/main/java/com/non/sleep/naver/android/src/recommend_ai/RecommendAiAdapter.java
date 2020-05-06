package com.non.sleep.naver.android.src.recommend_ai;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.non.sleep.naver.android.R;
import com.non.sleep.naver.android.src.recommend.models.ObjectResponse;
import com.non.sleep.naver.android.src.recommend_ai.models.ObjectResponse2;
import com.non.sleep.naver.android.src.shopping.ShoppingActivity;

import java.util.ArrayList;

public class RecommendAiAdapter extends RecyclerView.Adapter<RecommendAiAdapter.ViewHolder> {
    public ArrayList<ObjectResponse> mData = null ;
    Context mContext;
    RecommendListener mRecommendListener = null;

    // 생성자에서 데이터 리스트 객체를 전달받음.
    RecommendAiAdapter(ArrayList<ObjectResponse> list) {
        mData = list ;
    }
    RecommendAiAdapter(Context context, RecommendListener recommendListener){
        mData = new ArrayList<>();
        mContext = context;
        mRecommendListener = recommendListener;
    }

    public interface RecommendListener{
        void itemClick(int pos, String name, int menuNo);
    }

    public void addItem(String url, String title, int price) {
        ObjectResponse item = new ObjectResponse();

        item.setPrice(price);
        item.setImageUrl(url);
        item.setPrice(price);
        mData.add(item);
    }
    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public RecommendAiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.item_ai_recommend, parent, false) ;
        RecommendAiAdapter.ViewHolder vh = new RecommendAiAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(final RecommendAiAdapter.ViewHolder holder, final int position) {
        final ObjectResponse item = mData.get(position) ;

        Log.i("SVasdvdsa", mData.get(position).getName());
//        holder.title.setText(item.getName());

        Glide.with(mContext).load(item.getImageUrl()).into(holder.iv);

//        holder.iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(item.select == true) {
////                    if(mData.get(position) == null) {
////                        return;
////                    }
////                    if(RecommendAiActivity.won>0) {
////                        if(RecommendAiActivity.count ==1) {
////                            RecommendAiActivity.mTvName1.setText("");
////                            RecommendAiActivity.mTvWon1.setText("");
////                        }
////                        else if(RecommendAiActivity.count ==2) {
////                            RecommendAiActivity.mTvName2.setText("");
////                            RecommendAiActivity.mTvWon2.setText("");
////                        }
////                        else if(RecommendAiActivity.count ==3) {
////                            RecommendAiActivity.mTvName3.setText("");
////                            RecommendAiActivity.mTvWon3.setText("");
////                        }
////                        else if(RecommendAiActivity.count ==4) {
////                            RecommendAiActivity.mTvName4.setText("");
////                            RecommendAiActivity.mTvWon4.setText("");
////                        }
////                        RecommendAiActivity.won -= item.getPrice();
////                        RecommendAiActivity.mTvTotalPay.setText(String.valueOf(RecommendAiActivity.won) + "원");
////                        RecommendAiActivity.count--;
////                    }
//                }
//                else {
//                    Intent intent = new Intent(mContext, ShoppingActivity.class);
//                    mContext.startActivity(intent);
//                    if(RecommendAiActivity.count ==0) {
//                        RecommendAiActivity.mTvName1.setText(mData.get(position).getName());
//                        if (mData.get(position) != null) {
//                            RecommendAiActivity.mTvWon1.setText(String.valueOf(mData.get(position).getPrice()));
//                        }
//                    }
//                    else if(RecommendAiActivity.count ==1) {
//                        RecommendAiActivity.mTvName2.setText(mData.get(position).getName());
//                        if (mData.get(position) != null) {
//                            RecommendAiActivity.mTvWon2.setText(String.valueOf(mData.get(position).getPrice()));
//                        }
//                    }
//                    else if(RecommendAiActivity.count ==2) {
//                        RecommendAiActivity.mTvName3.setText(mData.get(position).getName());
//                        if (mData.get(position) != null) {
//                            RecommendAiActivity.mTvWon3.setText(String.valueOf(mData.get(position).getPrice()));
//                        }
//                    }
//                    else if(RecommendAiActivity.count ==3) {
//                        RecommendAiActivity.mTvName4.setText(mData.get(position).getName());
//                        if (mData.get(position) != null) {
//                            RecommendAiActivity.mTvWon4.setText(String.valueOf(mData.get(position).getPrice()));
//                        }
//                    }
//                    RecommendAiActivity.count++;
//                    RecommendAiActivity.won += item.getPrice();
//                    RecommendAiActivity.mTvTotalPay.setText(String.valueOf(RecommendAiActivity.won) + "원");
//                }
//                item.select = !item.select;
//            }
//        });

//        holder.icon.setImageDrawable(item.getIcon()) ;
//        holder.title.setText(item.getTitle()) ;
//        holder.desc.setText(item.getDesc()) ;
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv ;
        TextView title ;

        ViewHolder(View itemView) {
            super(itemView) ;


            // 뷰 객체에 대한 참조. (hold strong reference)
            iv = itemView.findViewById(R.id.recommend_ai_item_iv) ;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRecommendListener.itemClick(getAdapterPosition(), mData.get(getAdapterPosition()).getName(), mData.get(getAdapterPosition()).getMenuNo());
                }
            });
            title = itemView.findViewById(R.id.recommend_ai_item_tv) ;
        }
    }
}