import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.HistoryItem
import com.example.myapplication.R

class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgHistory: ImageView = itemView.findViewById(R.id.imgHistory)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtLoai: TextView = itemView.findViewById(R.id.txtLoai)
        val txtTimestamp: TextView = itemView.findViewById(R.id.txtTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        // Giải mã ảnh base64
        val imageBytes = Base64.decode(item.imagebase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.imgHistory.setImageBitmap(bitmap)

        holder.txtName.text = item.info.ten
        holder.txtLoai.text = item.info.loai
        holder.txtTimestamp.text = item.timestamp
    }

    override fun getItemCount(): Int = items.size
}
