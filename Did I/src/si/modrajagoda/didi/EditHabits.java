package si.modrajagoda.didi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import si.modrajagoda.didi.database.DatabaseHelper;
import si.modrajagoda.didi.database.Habit;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EditHabits extends ListActivity {
	
	private ArrayAdapter<String> adapter;
	private ArrayList<String> habitQuestions = null;
	private DatabaseHelper databaseHelper = null;
	private Dao<Habit, Integer> habitDao = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.edit_habits);
		setContentView(R.layout.edit_habits);
		
		databaseHelper = getHelper();
		habitQuestions = new ArrayList<String>();
		try {
			habitDao = databaseHelper.getHabitDao();
			List<Habit> habits = habitDao.queryForAll();
			for(Habit habit : habits){
				habitQuestions.add(habit.getName());
			}
			habitQuestions.add("");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ListView list = getListView();
		adapter = new CustomAdapter(this, R.layout.list_item_habit, habitQuestions);
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		list.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		        // Here you can perform updates to the CAB due to
		        // an invalidate() request
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
		        // Here you can make any necessary updates to the activity when
		        // the CAB is removed. By default, selected items are deselected/unchecked.
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		        // Inflate the menu for the CAB
		        MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.context, menu);
				return false;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
                for (int i = 0; i < checkedItems.size(); i++) {
                	switch (item.getItemId()) {
					case R.id.menu_delete:
						// TODO delete item
						break;
					}
                }
				mode.finish(); // Action picked, so close the CAB
				return false;
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				mode.setTitle(getString(R.string.selected_items)+" "+
					Integer.toString(getListView().getCheckedItemCount()));
				// TODO set background color
			}
		});
	}

	private class CustomAdapter extends ArrayAdapter<String> {
		private Context mContext;
		private List<String> habitQuestions;

		public CustomAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			this.mContext = context;
			this.habitQuestions = objects;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unnecessary calls to findViewById() on each row.
			ViewHolder holder;
			LayoutInflater li = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// When convertView is not null, we can reuse it directly, there is
			// no need to reinflate it. We only inflate a new View when the 
			// convertView supplied by ListView is null.
			if (convertView == null) {
				convertView = li.inflate(R.layout.list_item_habit, null);

				// Creates a ViewHolder and store references to children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.habitNumber = (TextView) convertView
						.findViewById(R.id.question_number);
				holder.habitName = (EditText) convertView
						.findViewById(R.id.habit_name);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView
				holder = (ViewHolder) convertView.getTag();
			}

			holder.habitNumber.setText(Integer.toString(position + 1)+".");
			holder.habitName.setText(habitQuestions.get(position));
			holder.habitName.setOnEditorActionListener(new OnEditorActionListener() {
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE && v.getText() != null){
						// After saving a question display a new one unless there are already 5
						habitQuestions.set(position, v.getText().toString());
						if(position == getListView().getCount()-1 && position != 4){
							habitQuestions.add("");
							adapter.notifyDataSetChanged();
						}
						
						// Save to database
						try {
							habitDao = databaseHelper.getHabitDao();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						if(habitDao != null){
							try {
								Habit habit = new Habit(v.getText().toString());
								habitDao.create(habit);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
					return false;
				}
			});

			return convertView;
		}
	}

	// ViewHolder for the efficient adapter
	private static class ViewHolder {
		TextView habitNumber;
		EditText habitName;
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}

	private DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper =
	            OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}

}