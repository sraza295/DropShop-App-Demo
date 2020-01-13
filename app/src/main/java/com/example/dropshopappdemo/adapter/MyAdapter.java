package com.example.dropshopappdemo.adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dropshopappdemo.R;
import com.example.dropshopappdemo.modal.Product;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter<P> extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
{
    List<Product> list;
    public MyAdapter(List list)
    {
        this.list=list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // create a new view
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.table_list_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        int rowPos = holder.getAdapterPosition();

       if (position == 0) {

            holder.productId.setText("Product Id");
            holder.productDesc.setText("Product Desc");
            holder.productCode.setText("Product Code");
            holder.mrp.setText("MRP");
            holder.customerId.setText("Customer Id");
            holder.brandName.setText("Brand Name");
            holder.brandCode.setText("Brand Code");
            holder.expiry.setText("Expiry");



        } else {

           Product product = list.get(position-1);
           System.out.println("hello "+product.getProductId());
           System.out.println("rowPos "+rowPos);
           holder.productId.setText(product.getProductId());
           holder.productDesc.setText(product.getProductDesc());
           holder.productCode.setText(product.getProductCode()+"");
           holder.mrp.setText(product.getMrp()+"");
           holder.customerId.setText(product.getCustomerId());
           holder.brandName.setText(product.getBrandName());
           holder.brandCode.setText(product.getBrandCode());
           holder.expiry.setText(convertDate(String.valueOf(product.getExpiry()),"dd/MM/yyyy hh:mm:ss"));

        }

    }

    @Override
    public int getItemCount() {
        return list.size()+1;
    }

    public static String convertDate(String dateInMilliseconds,String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView productId;
        public TextView productDesc;
        public TextView productCode;
        public TextView mrp;
        public TextView customerId;
        public TextView brandName;
        public TextView brandCode;
        public TextView expiry;

        public MyViewHolder(View v) {
            super(v);
            this.productId = (TextView) itemView.findViewById(R.id.productId);
            this.productDesc = (TextView) itemView.findViewById(R.id.productDesc);
            this.productCode = (TextView) itemView.findViewById(R.id.productCode);
            this.mrp = (TextView) itemView.findViewById(R.id.mrp);
            this.customerId = (TextView) itemView.findViewById(R.id.customerId);
            this.brandName = (TextView) itemView.findViewById(R.id.brandName);
            this.brandCode = (TextView) itemView.findViewById(R.id.brandCode);
            this.expiry = (TextView) itemView.findViewById(R.id.expiry);

        }
    }
}
