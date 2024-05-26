package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import pt.ulisboa.tecnico.cmov.pharmacist.pojo.Indexed;

public class AdapterUtils {
    public static void removeChild(String id, List<? extends Indexed> list, RecyclerView.Adapter adapter) {
        if (id == null) return;

        OptionalInt index = IntStream.range(0, list.size())
                .filter(i -> id.equals(list.get(i).getId()))
                .findFirst();

        if (!index.isPresent()) return;

        list.remove(index.getAsInt());
        adapter.notifyItemRemoved(index.getAsInt());
    }
}
