package com.example.timertest;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    int mCount = 0;
    int tick_cnt = 0;
    public static final int TICK_SPEED = 500, MAX_SPEED=3000;
    public static final int INDEX_PAGE = 0, INDEX_ROW = 1, INDEX_COL = 2;
    public static final int NUM_PAGE = 2, NUM_ROW = 5, NUM_COL = 4, NUM_INDICATE = 3;
    public static final int[] NUM_INDEX = {NUM_PAGE,NUM_ROW,NUM_COL};
    int State_run = 0; // running state
    int Indicate = 0;  // pointer of depth
    int[] State = {0,0,0}; // page, row, col
    int Selected = 0;  // flag of select button
    int SPEED = 1500;  // msec of timeout
    int Mode = 0; // 0:manual , 1:automatic
    //public static final int SEND_START = 1;
    //public static final int SEND_STOP = 0;

    TextView mTextMessage;
    Button BtnStart, BtnSelect, BtnClear, BtnSpeed;
    //EditText mText1;

    TextView[][] card_view = new TextView[NUM_ROW][NUM_COL];

    int[][]  card_id = {{R.id.card_00, R.id.card_01, R.id.card_02, R.id.card_03}
                        ,{R.id.card_10, R.id.card_11, R.id.card_12, R.id.card_13}
                        ,{R.id.card_20, R.id.card_21, R.id.card_22, R.id.card_23}
                        ,{R.id.card_30, R.id.card_31, R.id.card_32, R.id.card_33}
                        ,{R.id.card_40, R.id.card_41, R.id.card_42, R.id.card_43}};
    char[][][] card_text = {{{'ㄱ','ㄴ','ㄷ','ㄹ'}
                        ,{'ㅁ','ㅂ','ㅅ','ㅇ'}
                        ,{'ㅈ','ㅊ','ㅋ','ㅌ'}
                        ,{'ㅍ','ㅎ','ㄲ','ㄸ'}
                        ,{'ㄸ','ㅉ','ㅃ','☆'}}
                        ,{{'ㅏ','ㅑ','ㅓ','ㅕ'}
                        ,{'ㅗ','ㅛ','ㅜ','ㅠ'}
                        ,{'ㅡ','ㅣ','ㅚ','ㅟ'}
                        ,{'ㅢ','ㅐ','ㅔ','ㅖ'}
                        ,{'ㅘ','ㅝ','ㅙ','ㅞ'}}};

    public int wait_and_check_select() {
        for (int i=0; i<SPEED/100; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (State_run == 1 && Selected == 1)
                return 1; // Selected
            else if (State_run == 0)
                return -1; // running stopped
        }
        return 0; // timeout
    }
    public void display_textmessage(int append) {
        if (append == 1)
            mTextMessage.append(card_text[State[INDEX_PAGE]][State[INDEX_ROW]][State[INDEX_COL]] + "");
        else
            mTextMessage.setText("");
    }
    public void card_set_text(int page) {
        for (int i=0; i<NUM_ROW; i++) {
            for (int j=0; j<NUM_COL; j++) {
                card_view[i][j].setText(card_text[page][i][j] + "");
            }
        }
    }
    public void card_set_color(int type) {
        //int color_background = getResources().getColor(R.color.card_background);
        //int color_highlight = getResources().getColor(R.color.card_highlight);
        int color;

        if (type == 1)
            color = getResources().getColor(R.color.card_highlight);
        else if (type == 2)
            color = getResources().getColor(R.color.card_green);
        else
            color = getResources().getColor(R.color.card_background);

        for (int i=0; i<NUM_ROW; i++) {
            for (int j=0; j<NUM_COL; j++) {
                card_view[i][j].setBackgroundColor(color);
            }
        }
    }
    public void card_update_color () {
        int color_background = getResources().getColor(R.color.card_background);
        int color_highlight = getResources().getColor(R.color.card_highlight);
        int color = color_background;
        if (Indicate == 0)
            color = color_highlight;
        for (int i=0; i<NUM_ROW; i++) {
            if (Indicate == 1)
                if (State[1] == i)
                    color = color_highlight;
                else
                    color = color_background;
            for (int j = 0; j < NUM_COL; j++) {
                if (Indicate == 2)
                    if (State[1] == i && State[2] == j)
                        color = color_highlight;
                    else
                        color = color_background;
                card_view[i][j].setBackgroundColor(color);
            }
        }
    }
    public void init_card_state(int page) {
        Indicate = 0;
        State[INDEX_PAGE] = page;
        State[INDEX_ROW] = NUM_ROW -1;
        State[INDEX_COL] = NUM_COL -1;
        //card_set_text(State[INDEX_PAGE]);
    }
    public void go_next_state () {
        if (++State[Indicate] >= NUM_INDEX[Indicate])
            State[Indicate] = 0;
        if (Indicate == INDEX_PAGE)
            card_set_text(State[INDEX_PAGE]);
    }
    public void select_card() {
        if (State_run == 1 || Mode == 0) {
            if (State_run == 1)
                Tick.removeMessages(0);
            if (++Indicate < NUM_INDICATE) // go into depth
                State[Indicate] = 0; //NUM_INDEX[Indicate] -1;
            else { // display text and go next page
                display_textmessage(1);
                if (++State[INDEX_PAGE] >= NUM_PAGE)
                    State[INDEX_PAGE] = 0;
                init_card_state(State[INDEX_PAGE]);
                card_set_text(State[INDEX_PAGE]);
                //Indicate = 0;
            }
            if (Mode == 0)
                card_update_color();
            if (State_run == 1) {
                //tick_cnt = SPEED/TICK_SPEED -1;
                tick_cnt = -1;
                Tick.sendEmptyMessage(0);
            }
        }
    }
    Handler Tick = new Handler() {
        public void handleMessage(Message msg) {
            if (State_run == 1) {
                if (((++tick_cnt)*TICK_SPEED) >= SPEED) {
                    tick_cnt = 0;
                    go_next_state();
                }
                card_update_color();
                Tick.sendEmptyMessageDelayed(0, TICK_SPEED);
            }
        }
    };
    Handler mainloop = new Handler() {
        public void handleMessage(Message msg) {
            System.out.println("start of mainloop");

/*
            card_set_text(0);
            card_set_color(1);
            wait_and_check_select();
            card_set_color(0);
*/

            while (State_run == 1) {
                card_set_text(State[INDEX_PAGE]);
                card_update_color();
                int res = wait_and_check_select();
                if (res == 1) { // Selected
                    select_card();
                } else if (res == 0) { // timeout
                    go_next_state();
                }
            }

            System.out.println("end of mainloop");
        }
    };
            Handler mTimer = new Handler() {
        public void handleMessage(Message msg) {
            //mTextMessage.setText("Count = "+ mCount);
            //mBtnA1.setBackgroundResource(mBtnImage[mCount % 3]);

            if (State_run == 1)
                if (wait_and_check_select() == 1) {
                    mTextMessage.append(card_text[State[0]][State[1]][State[2]] + "");
                    Indicate = 0;
                    //State[0] = 0;
                    State[1] = -1;
                    State[2] = -1;
                }

            if (Indicate < NUM_INDICATE - 1)
                State[++Indicate] = -1;
            else {
                mTextMessage.append(card_text[State[0]][State[1]][State[2]] + "");
                Indicate = 0;
                //State[0] = 0;
                State[1] = -1;
                State[2] = -1;
            }

            if (State_run == 1) {
                go_next_state();
                card_set_text(State[0]);
                card_update_color();

                mTimer.sendEmptyMessageDelayed(0, SPEED);

                /*
                if (mCount % 2 == 0) {
                    //card_view[0][0].setText("ㄱ");
                    //card_view[0][0].setText(char_con[0][0] + "");
                    //card_view[0][0].setBackgroundColor(Color.rgb(255,255,255));
                    card_set_text(0);

                } else {
                    //card_view[0][0].setText("ㅏ");
                    //card_view[0][0].setText(char_con[0][0] + "");
                    //card_view[0][0].setBackgroundColor(Color.rgb(0,255,0));
                    card_set_text(1);
                }
                */
                //mCount++;
                //mTimer.sendEmptyMessageDelayed(0, 1000);
            }
        }
    };
    public void trigger_auto_mode() {
        // check automatic mode
        if (Mode != 1)
            return;
        if (State_run == 0) {
            State_run = 1;
            init_card_state(NUM_PAGE-1);
            tick_cnt = SPEED/TICK_SPEED -1;
            Tick.sendEmptyMessage(0); // Run tick loop
            BtnStart.setText("정지");
            BtnSelect.setText("선택");
        } else {
            Tick.removeMessages(0);
            State_run = 0;
            card_set_text(0);
            card_set_color(0);
            BtnStart.setText("자동");
            BtnSelect.setText("시작");
        }
    }
    public void initial_state() {
        card_set_text(0);
        card_set_color(0);
        init_card_state(NUM_PAGE-1);
    }
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextMessage =(TextView)findViewById(R.id.textMessage);
        BtnStart = (Button)findViewById(R.id.btn_start);
        BtnSelect = (Button)findViewById(R.id.btn_select);
        BtnClear = (Button)findViewById(R.id.btn_clear);
        BtnSpeed = (Button)findViewById(R.id.btn_speed);

        for (int i=0; i<NUM_ROW; i++) {
            for (int j=0; j<NUM_COL; j++) {
                card_view[i][j] = findViewById(card_id[i][j]);
            }
        }

        // Init
        initial_state();
        //card_set_text(0);
        //card_set_color(0);
        //init_card_state(NUM_PAGE-1);
        Mode = 0; // manual mode

        // START
        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("BtnStart.onClick++");
                if (Mode == 0) {
                    Mode = 1;
                    initial_state();
                    BtnStart.setText("자동");
                    BtnSelect.setText("시작");
                    BtnSpeed.setText((SPEED/1000)+"."+((SPEED%1000)/100));
                } else if (State_run == 1) {
                    trigger_auto_mode();
                } else {
                    Mode = 0;
                    BtnStart.setText("수동");
                    BtnSelect.setText("시작");
                    BtnSpeed.setText("선택");
                }
                System.out.println("BtnStart.onClick--");
            }
        });

        // SELECT
        BtnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("start of onclick.select");
                if (Mode == 0) {
                    // manual mode
                    go_next_state();
                    card_update_color();
                    BtnSelect.setText("다음");
                } else if (Mode == 1) {
                    // automatic mode
                    if (State_run == 0)
                        trigger_auto_mode();
                    else
                        select_card();
                    //select_card();
                }
                System.out.println("end of onclick.select");
            }
        });
        // SELECT long click
        /*
        BtnSelect.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        */
        //Start Select Speed
        //(수동) (다음) (선택)
        //(자동) (시작) (1.5)
        //(정지) (선택) (1.5)
        // SPEED
        BtnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Mode == 0) {
                    select_card();
                } else {
                    SPEED -= TICK_SPEED;
                    if (SPEED < TICK_SPEED)
                        SPEED = MAX_SPEED;
                    BtnSpeed.setText((SPEED/1000)+"."+((SPEED%1000)/100));
                }
            }
        });

        BtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display_textmessage(0);
            }
        });
        //mTimer.sendEmptyMessage(0);
        //mTimer.sendEmptyMessageDelayed(0,1000);

/*
        // for test
        mBtnImage[0] = R.drawable.btn_1;
        mBtnImage[1] = R.drawable.btn_2;
        mBtnImage[2] = R.drawable.btn_3;

        mBtnA1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextMessage.setText("Hello World!");
                mTextMessage.setB
                //mTextMessage.append("ㄱ");
                //mText1.append("ㄱ");
                //mText1.setText(mText1.getText().toString() + "ㄱ");
            }
        });
        mBtnA2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextMessage.append("ㄴ");
                //mText1.append("ㄴ");
                //mText1.setText(mText1.getText().toString() + "ㄴ");
            }
        });
        mBtnA3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextMessage.append("ㅏ");
                //mText1.append("ㅏ");
                //mText1.setText(mText1.getText().toString() + "ㅏ");
            }
        });
        mBtnA4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextMessage.append("ㅡ");
                //mText1.append("ㅡ");
                //mText1.setText(mText1.getText().toString() + "ㅡ");
            }
        });
*/
    }
}
