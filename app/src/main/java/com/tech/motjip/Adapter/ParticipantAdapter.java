package com.tech.motjip.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tech.motjip.Model.Participant;
import com.tech.motjip.R;

import java.util.List;

public class ParticipantAdapter
        extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private final List<Participant> participantList;

    private final OnParticipantClickListener listener;

    public interface OnParticipantClickListener {

        void onParticipantClick(
                Participant participant
        );
    }

    public ParticipantAdapter(
            List<Participant> participantList
    ) {

        this.participantList =
                participantList;

        this.listener =
                null;
    }

    public ParticipantAdapter(
            List<Participant> participantList,
            OnParticipantClickListener listener
    ) {

        this.participantList =
                participantList;

        this.listener =
                listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                                parent.getContext()
                        )
                        .inflate(
                                R.layout.item_participant,
                                parent,
                                false
                        );

        return new ViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Participant participant =
                participantList.get(
                        position
                );

        String nickname =
                participant.getNickname();

        if (nickname == null
                || nickname.trim().isEmpty()) {

            nickname =
                    "사용자";
        }

        Log.d(
                "PARTICIPANT_TEST",
                nickname
        );

        holder.tvNickname.setText(
                nickname
        );

        Glide.with(
                        holder.itemView.getContext()
                )
                .load(
                        participant.getProfileImgUrl()
                )
                .placeholder(
                        R.drawable.people
                )
                .error(
                        R.drawable.people
                )
                .into(
                        holder.ivProfile
                );

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onParticipantClick(
                        participant
                );
            }
        });
    }

    @Override
    public int getItemCount() {

        if (participantList == null) {

            return 0;
        }

        return participantList.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvNickname;

        ImageView ivProfile;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(
                    itemView
            );

            ivProfile =
                    itemView.findViewById(
                            R.id.iv_participant_profile
                    );

            tvNickname =
                    itemView.findViewById(
                            R.id.tv_participant_nickname
                    );
        }
    }
}