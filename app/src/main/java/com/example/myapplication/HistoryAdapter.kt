import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.HistoryItem
import com.example.myapplication.R

class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image)
        val labelView: TextView = itemView.findViewById(R.id.label)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val accuracyView: TextView = itemView.findViewById(R.id.accuracyView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.labelView.text = item.info.ten
        holder.timeView.text = item.timestamp

        // Tạo URL đầy đủ từ đường dẫn ảnh
        val imageUrl = "http://192.168.1.139:5000" + item.image_base64  // Thay IP cho đúng

        // Tải ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.error)  // Ảnh chờ khi đang tải
            .error(R.drawable.error)          // Ảnh khi tải thất bại
            .into(holder.imageView)

        val accuracy = item.top3[0].score
        holder.accuracyView.text = "Độ chính xác: %.2f%%".format(accuracy)
    }


    override fun getItemCount(): Int = items.size
}
