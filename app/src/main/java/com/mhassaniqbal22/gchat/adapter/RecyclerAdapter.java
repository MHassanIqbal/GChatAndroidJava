package com.mhassaniqbal22.gchat.adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mhassaniqbal22.gchat.R;
import com.mhassaniqbal22.gchat.helper.CircleTransform;
import com.mhassaniqbal22.gchat.helper.FlipAnimator;
import com.mhassaniqbal22.gchat.model.ChatRoom;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    private Context mContext;
    private List<ChatRoom> rooms;
    private RecyclerAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView title, creator, message, timestamp, iconText;
        ImageView imgProfile;
        LinearLayout messageContainer;
        RelativeLayout iconContainer, iconBack, iconFront;

        RecyclerAdapterListener listener;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txt_title);
            creator = itemView.findViewById(R.id.txt_creator);
            message = itemView.findViewById(R.id.txt_message);
            iconText = itemView.findViewById(R.id.icon_text);
            timestamp = itemView.findViewById(R.id.timestamp);
            iconBack = itemView.findViewById(R.id.icon_back);
            iconFront = itemView.findViewById(R.id.icon_front);
            imgProfile = itemView.findViewById(R.id.icon_profile);
            messageContainer =  itemView.findViewById(R.id.message_container);
            iconContainer = itemView.findViewById(R.id.icon_container);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onRowLongClicked(getAdapterPosition());
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    return true;
                }
            });
        }
    }

    public RecyclerAdapter(Context mContext, List<ChatRoom> rooms, RecyclerAdapterListener listener) {
        this.mContext = mContext;
        this.rooms = rooms;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_room_list, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ChatRoom room = rooms.get(position);

        // displaying text view data
        holder.title.setText(room.getTitle());
        holder.creator.setText(room.getCreator());
        holder.message.setText(room.getMessage());
        holder.timestamp.setText(room.getTimestamp());

        // displaying the first letter of From in icon text
        holder.iconText.setText(room.getTitle().substring(0, 1));

        // change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        // change the font style depending on message read status
        applyReadStatus(holder, room);

        // handle icon animation
        applyIconAnimation(holder, position);

        // display profile image
        applyProfilePicture(holder, room);

        // apply click events
        applyClickEvents(holder, position);
    }

    private void applyClickEvents(ViewHolder holder, final int position) {
        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconClicked(position);
            }
        });


        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });

        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }

    private void applyProfilePicture(ViewHolder holder, ChatRoom room) {
        if (!TextUtils.isEmpty(room.getPicture())) {
            Glide.with(mContext).load(room.getPicture())
                    .thumbnail(0.5f)
                    .crossFade()
                    .transform(new CircleTransform(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgProfile);
            holder.imgProfile.setColorFilter(null);
            holder.iconText.setVisibility(View.GONE);
        } else {
            holder.imgProfile.setImageResource(R.drawable.bg_circle);
            holder.imgProfile.setColorFilter(room.getColor());
            holder.iconText.setVisibility(View.VISIBLE);
        }
    }

    private void applyIconAnimation(ViewHolder holder, int position) {
        if (selectedItems.get(position, false)) {
            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
                FlipAnimator.flipView(mContext, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }

    // As the views will be reused, sometimes the icon appears as
    // flipped because older view is reused. Reset the Y-axis to 0
    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(rooms.get(position).getId());
    }

    private void applyReadStatus(ViewHolder holder, ChatRoom room) {
        if (room.isRead()) {
            holder.title.setTypeface(null, Typeface.NORMAL);
            holder.creator.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(ContextCompat.getColor(mContext, R.color.subject));
            holder.creator.setTextColor(ContextCompat.getColor(mContext,R.color.subject ));
        } else {
            holder.title.setTypeface(null, Typeface.BOLD);
            holder.creator.setTypeface(null, Typeface.BOLD);
            holder.title.setTextColor(ContextCompat.getColor(mContext, R.color.title));
            holder.creator.setTextColor(ContextCompat.getColor(mContext, R.color.title));
        }
    }


    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void toggleSelection(int pos) {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        rooms.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface RecyclerAdapterListener {
        void onIconClicked(int position);

        void onMessageRowClicked(int position);

        void onRowLongClicked(int position);
    }
}
