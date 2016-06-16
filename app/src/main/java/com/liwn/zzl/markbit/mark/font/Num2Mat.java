package com.liwn.zzl.markbit.mark.font;// ֻ����95������ʾASCII����

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;


public class Num2Mat {
	private int font_width = 8;
	private int font_height = 12;
	private int size_step = 8;
	private char char_num = '1';
	private byte[] cbuf;
	private boolean[][] mat;
	private char[] key = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
	private Context context;
	
	Num2Mat(int font_height, int font_width, char char_num, Context context) {
		this.font_height = font_height;
		this.font_width = font_width;
		this.char_num = char_num;
		this.context = context;
	}
	
	public void setWord(char word) {
		this.char_num = word;
	}
	
	public void setFontSize(int font_height, int font_width) {
		this.font_height = font_height;
		this.font_width = font_width;
	}
	
	public void readFromLib() {
		int offset_step = font_width * font_height / size_step;

		int ascii = (int) char_num;
		if (ascii > 127 || ascii < 32) {
			System.out.println("input char is invaild!");
			return;
		}
		int offset = (ascii - 32) * offset_step;
		
		try {
			
			cbuf = new byte[offset_step];
			InputStream inputStream = context.getResources().getAssets().open("fontLib/" + "ASC" + String.valueOf(font_height) + "_" + String.valueOf(font_width));
			inputStream.skip(offset);
			if (inputStream.read(cbuf, 0, offset_step) < 0) {
				System.out.println("read failed!");
				inputStream.close();
				return;
			}

			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean[][] getMat() {
		readFromLib();
		if (font_height != 12 && font_height != 48) {
			mat = new boolean[font_height][font_width];
			// ����ȡ��
			for (int i = 0; i < font_height; i++) {
				for (int j = 0; j < font_width; j++) {
					int index = j * font_height + i;
					int flag = (cbuf[index / size_step] & key[index % size_step]);
					mat[i][j] = flag > 0 ? true : false;
				}
			}
		} else {
			mat = new boolean[font_height][font_width];
			// ����ȡ��
			for (int i = 0; i < font_height; i++) {
				for (int j = 0; j < font_width; j++) {
					int index = i * font_width + j;
					int flag = (cbuf[index / size_step] & key[index % size_step]);
					mat[i][j] = flag > 0 ? true : false;
				}
			}
		}
		return mat;
	}
	
	public void printMat() {
		for (int i = 0; i < font_height; i++) {
			for (int j = 0; j < font_width; j++) {
				System.out.print(mat[i][j] ? "��" : "��");
			}
			System.out.println();
		}
	}
	
	public void print() {
		if (font_height == 12 || font_height == 48) {
			// ����ȡ��
			for (int i = 0; i < font_height; i++) {
				for (int j = 0; j < font_width; j++) {
					// int index = j * font_height + i;
					int index = i * font_width + j;
					int flag = cbuf[index / size_step] & key[index % size_step];
					System.out.print(flag > 0 ? "��" : "��");
				}
				System.out.println();
			}
		} else {
			// ����ȡ��
			for (int i = 0; i < font_height; i++) {
				for (int j = 0; j < font_width; j++) {
					int index = j * font_height + i;
					int flag = cbuf[index / size_step] & key[index % size_step];
					System.out.print(flag > 0 ? "��" : "��");
				}
				System.out.println();
			}
		}
	}
}
