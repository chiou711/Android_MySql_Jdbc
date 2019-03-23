package com.cw.android_mysql_jdbc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cw.android_mysql_jdbc.test.R;
import com.google.android.youtube.player.YouTubeIntents;

public class MainActivity extends AppCompatActivity {

    private ListView list;
    private String [] Id;
    private String [] Uri;
    private String [] Title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("MainActivity / _onCreate");

        MyTask task = new MyTask();
        task.execute();

        list = findViewById(R.id.list_data);
    }

    private class MyTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Connection conn = null;

            try{
                System.out.println("MainActivity / MyTask /_doInBackground / try");

                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://10.1.1.3:3306/LiteNote";
	            String user = "guest";
	            String password = "1234";
                conn = DriverManager.getConnection(url,user,password);

                Statement st = conn.createStatement();

                //SQL QUERY COMMANDLINE
                String sql = "SELECT * FROM data_table";

                final ResultSet rs = st.executeQuery(sql);

                int size = 0;
                //Get data size
                while(rs.next()){
                    size++;
                }

                //Set Array size
                Id = new String[size];
                Uri = new String[size];
                Title = new String[size];


                //Move to first data
                rs.first();

                //Store data to array
                int i=0;
                do
                {
                    Id[i] = rs.getString(1);
                    Uri[i] = rs.getString(2);
                    Title[i] = rs.getString(3);
                    i++;
                }while(rs.next());


                conn.close();

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void Result){
            MyAdapter adapter = new MyAdapter(MainActivity.this);
            list.setAdapter(adapter);
            super.onPostExecute(Result);
        }
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        public MyAdapter(Context c){
            inflater = LayoutInflater.from(c);
        }
        @Override
        public int getCount() {
            if(Id == null)
                return 0;
            else
                return Id.length;
        }

        @Override
        public Object getItem(int position) {
            return Id[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.layout_adapter,null);
            TextView aName,aAddress,aTitle;

            aName = (TextView) view.findViewById(R.id.text_id);

            aName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = Uri[position];

                    Intent intent = null;
                    String idStr = getYoutubeId(uri);
                    intent = YouTubeIntents.createPlayVideoIntentWithOptions(MainActivity.this, idStr, false/*fullscreen*/, true/*finishOnEnd*/);
                    startActivity(intent);

                    //Toast.makeText(MainActivity.this,uri,Toast.LENGTH_SHORT).show();
                }
            });

            aAddress = (TextView) view.findViewById(R.id.text_uri);
            aTitle = (TextView) view.findViewById(R.id.text_title);

            aName.setText(Id[position]);
            aAddress.setText(Uri[position]);
            aTitle.setText(Title[position]);
            return view;
        }
    }


    // Get YouTube Id
    public static String getYoutubeId(String url) {

        String videoId = "";

        // format 1: https://www.youtube.com/watch?v=_sQSXwdtxlY
        // format 2: https://youtu.be/V7MfPD7kZuQ (notice: start with V)
        if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
            String expression = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??(v=)?([^#\\&\\?]*).*";
            CharSequence input = url;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(8);
                if (groupIndex1 != null && groupIndex1.length() == 11)
                    videoId = groupIndex1;
            }
        }
        return videoId;
    }
}