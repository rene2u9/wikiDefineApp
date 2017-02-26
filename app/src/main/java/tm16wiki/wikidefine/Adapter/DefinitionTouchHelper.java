package tm16wiki.wikidefine.Adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by rene2 on 23.02.2017.
 */

public class DefinitionTouchHelper extends ItemTouchHelper.SimpleCallback {
        private MyRecyclerViewAdapter mAdapter;

        public DefinitionTouchHelper(MyRecyclerViewAdapter  movieAdapter){
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.mAdapter = movieAdapter;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //TODO: Not implemented here
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //Remove item
            mAdapter.deleteItem(viewHolder.getAdapterPosition());
        }
}
