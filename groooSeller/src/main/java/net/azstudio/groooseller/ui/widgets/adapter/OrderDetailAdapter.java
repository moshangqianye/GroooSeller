package net.azstudio.groooseller.ui.widgets.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.azstudio.groooseller.R;
import net.azstudio.groooseller.model.business.FoodOrder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by runzii on 16-5-25.
 */
public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.DetailHolder> {

    List<FoodOrder.DetailBean> details;

    public OrderDetailAdapter(List<FoodOrder.DetailBean> list) {
        this.details = list;
    }

    @Override
    public DetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new DetailHolder(v);
    }

    @Override
    public void onBindViewHolder(DetailHolder holder, int position) {
        FoodOrder.DetailBean bean = details.get(position);
        holder.name.setText(bean.getName());
        holder.count.setText(bean.getCount() + "");
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public static class DetailHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.detail_item_name)
        TextView name;
        @BindView(R.id.detail_item_count)
        TextView count;

        public DetailHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
