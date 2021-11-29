package com.project.contents;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ContentsDTO {
	  private int contentsno;
	  private String id;
	  private String filename;
	  private MultipartFile filenameMF;
	  private String contents;
	  private String rdate;
	  private int likecnt;
	  private int privacy;
	  private int tag_id;
	  private String tag;
	  private double x_site;
	  private double y_site;
	  private String fname;
	  private int like_clicked;
	  private List<String> tag_list;
	  
	  private String rnum;
	  private String reply;
	  
<<<<<<< HEAD:src/main/java/com/project/contents/ContentsDTO.java
	  public void setRnum(String contentsno) {
		// TODO Auto-generated method stub
		
	}

=======
>>>>>>> rereply:workspace/zagook/src/main/java/com/project/contents/ContentsDTO.java
}
