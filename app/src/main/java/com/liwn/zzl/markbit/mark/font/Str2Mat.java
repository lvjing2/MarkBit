package com.liwn.zzl.markbit.mark.font;

import android.content.Context;

public class Str2Mat {

	private String input;
	private Char2Mat cm;
	private Num2Mat nm;
	
	private int screen_height;
	private int screen_width;
	private int screen_num_height;
	private int screen_num_width;
	private int screen_num;
	private int screen_sub_height;
	private int screen_sub_width;
	private boolean[][] screen;
	private boolean[][][] sub_screen;
	private boolean isDigit;
	private boolean isChinese;

	private Context context;
	
	Str2Mat(String input, int screen_sub_height, int screen_sub_width, int screen_num_height, int screen_num_width, boolean isDigit, boolean isChinese, Context context) {
		this.input = input;
		this.screen_height = screen_sub_height * screen_num_height;
		this.screen_width = screen_sub_width * screen_num_width;
		this.screen_num_height = screen_num_height;
		this.screen_num_width = screen_num_width;
		this.screen_num = screen_num_height * screen_num_width;
		this.screen_sub_height = screen_sub_height;
		this.screen_sub_width = screen_sub_width;

		this.context = context;
		screen = new boolean[screen_height][screen_width];
		sub_screen = new boolean[screen_num][screen_sub_height][screen_sub_width];
		
		this.isDigit = isDigit;
		this.isChinese = isChinese;
		cm = new Char2Mat(32, '啊', context);
		nm = new Num2Mat(32, 16, 'A', context);
	}
	
	public boolean[][] getMat() {
		if (isDigit) {
			getSubScreenDigit();
			jointScreen();
		} else if (isChinese) {
			getSubScreenChinese();
			jointScreen();
		}
		
		return screen;
	}
	
	public void printScreen(boolean[][] mat, int height, int width) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				System.out.print(mat[i][j] ? "��" : "��");
			}
			System.out.println();
		}
	}
	
	public void getSubScreenDigit() {
		for (int i = 0; i < screen_num_height; ++i) {
			for (int j = 0; j < screen_num_width; ++j) {
				nm.setFontSize(screen_sub_height, screen_sub_width);
				nm.setWord(input.charAt(i*screen_num_width + j));
				sub_screen[i*screen_num_width + j] = nm.getMat();
			}
		}
	}
	
	public void getSubScreenChinese() {
		for (int i = 0; i < screen_num_height; ++i) {
			for (int j = 0; j < screen_num_width; ++j) {
				cm.setFontSize(screen_sub_height);
				cm.setWord(input.charAt(i*screen_num_width + j));
				sub_screen[i*screen_num_width + j] = cm.getMat();
			}
		}
	}

	public void jointScreen() {
		
		for (int i = 0; i < screen_num_height; ++i) {
			for (int j = 0; j < screen_num_width; ++j) {
				for (int k = 0; k < screen_sub_height; k++) {
					for (int l = 0; l < screen_sub_width; l++) {
						screen[i*screen_sub_height + k][j*screen_sub_width + l] = sub_screen[i*screen_num_width + j][k][l];
					}
				}
			}
		}
	}
}
