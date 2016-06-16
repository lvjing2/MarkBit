package com.liwn.zzl.markbit.mark.font;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;


public class Char2Mat {
	private int font_size = 48;
	private int font_height = font_size;
	private int font_width = font_size;
	private int size_step = 8;
	private char word = '啊';

	private byte[] cbuf;
	private boolean[][] mat;
	private char[] key = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
	private Context context;


	public void setFontSize(int font_size) {
		this.font_size = font_size;
		this.font_height = font_size;
		if (font_size == 12)
			this.font_width = 16;
		else
			this.font_width = font_size;
	}
	
	public void setWord(char word) {
		this.word = word;
	}
	
	Char2Mat(int font_size, char word, Context context) {
		this.font_size = font_size;
		this.font_height = font_size;
		if (font_size == 12)
			this.font_width = 16;
		else
			this.font_width = font_size;
		this.word = word;
		this.context = context;
	}
	
	public void readFromLib() {
		try {
			int sizeof_byte = size_step;
			int offset_step = font_width * font_height / sizeof_byte;

			byte[] incode = String.valueOf(word).getBytes("GB2312");
			int t1 = (int) (incode[0] & 0xff);
			int t2 = (int) (incode[1] & 0xff);
			int offset = 0;

			// calculate offset for different size font
			if (t1 > 0xa0) {
				offset = ((t1 - 0xa1) * 94 + (t2 - 0xa1)) * offset_step;
			} else {
				offset = (t1 + 156 - 1) * offset_step;
			}

			cbuf = new byte[offset_step];
			InputStream inputStream = context.getResources().getAssets().open("fontLib/" + "HZK"
					+ String.valueOf(font_size));
//			FileInputStream inputStream = new FileInputStream("HZK"
//					+ String.valueOf(font_size));
			inputStream.skip(offset);
			if (inputStream.read(cbuf, 0, offset_step) < 0) {
				System.out.println("read failed!");
				inputStream.close();
				return;
			}

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean[][] getMat() {
		readFromLib();
		if (font_size == 12 || font_size == 16 || font_size == 24 || font_size == 32) {
//			mat = new byte[offset_step];
			mat = new boolean[font_height][font_width];
			// ����ȡ��
			for (int i = 0; i < font_size; i++) {
				for (int j = 0; j < font_size; j++) {
					int index = j * font_width + i;
					int flag = (cbuf[index / size_step] & key[index % size_step]);
					mat[i][j] = flag > 0 ? true : false;
				}
			} 
		} else if (font_size == 40 || font_size == 48) {
			// mat = new byte[offset_step];
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
		for (int i = 0; i < font_size; i++) {
			for (int j = 0; j < font_size; j++) {
				System.out.print(mat[i][j] ? "��" : "��");
			}
			System.out.println();
		}
	}
	
	public void print() {
		if (font_size == 40 || font_size == 48) {
			for (int i = 0; i < font_size; i++) {
				for (int j = 0; j < font_width; j++) {
					int index = i * font_width + j;
					int flag = cbuf[index / size_step] & key[index % size_step];
					System.out.print(flag > 0 ? "��" : "��");
				}
				System.out.println();
			}
		} else if (font_size == 12 || font_size == 16 || font_size == 24 || font_size == 32) {
			for (int i = 0; i < font_size; i++) {
				for (int j = 0; j < font_size; j++) {
					int index = j * font_width + i;
					int flag = cbuf[index / size_step] & key[index % size_step];
					System.out.print(flag > 0 ? "��" : "��");
				}
				System.out.println();
			}
		}
	}
}
