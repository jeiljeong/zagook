package com.project.contents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.Utility.Utility;
import com.project.feed.FeedDTO;
import com.project.member.Member;
import com.project.reply.ReplyDTO;
import com.project.reply.ReplyService;

@Controller
public class ContentsController {
	@Autowired
	@Qualifier("com.project.contents.ContentsServiceImpl")
	private ContentsService service;
	
	@Autowired
	@Qualifier("com.project.reply.ReplyServiceImpl")
	private ReplyService replyService;

	@GetMapping("/searching")
	public String home(HttpServletRequest request, HttpSession session) {
		if (session.getAttribute("id") != null) {
			Map map = new HashMap();
			String id = (String) session.getAttribute("id");
			Double x_site = (Double) session.getAttribute("x_site");
			Double y_site = (Double) session.getAttribute("y_site");
			map.put("x_site", x_site);
			map.put("y_site", y_site);
			map.put("id", id);

			List<ContentsDTO> list = service.list(map);
			List<String> tag_list = new ArrayList();

			request.setAttribute("list", list);

			int k = 0;
			while (k < list.size()) {
				int cnt = 0;
				int check = 0;
				ContentsDTO dto = list.get(k);
				map.put("contentsno", dto.getContentsno());

				
				check = service.likeCheck(map); 
				if(check > 0) {
					dto.setLike_clicked(check);
				}

				tag_list = service.getTag(dto.getContentsno());
				dto.setTag_list(tag_list);
				k++;
			}

		}
		return "/searching";
	}
	
	@GetMapping(value = "/get_center", produces = "application/json")
	@ResponseBody
	public List<ContentsDTO> get_center(HttpServletRequest request, HttpSession session) throws IOException {
		Double x_site = Double.parseDouble(request.getParameter("x_site"));
		Double y_site = Double.parseDouble(request.getParameter("y_site"));
		String id = (String) session.getAttribute("id");
		Map map = new HashMap();
		map.put("x_site", x_site);
		map.put("y_site", y_site);
		map.put("id", id);
		List<ContentsDTO> searchlist = service.list(map);
		List<String> tag_list = new ArrayList();
		int k = 0;
		while (k < searchlist.size()) {
			int cnt = 0;
			int check = 0;
			ContentsDTO dto = searchlist.get(k);
			map.put("contentsno", dto.getContentsno());

			
			check = service.likeCheck(map); 
			if(check > 0) {
				dto.setLike_clicked(check);
			}

			tag_list = service.getTag(dto.getContentsno());
			System.out.println(tag_list);
			dto.setTag_list(tag_list);
			k++;
		}
		return searchlist;
	}

	@GetMapping("/contents/create")
	public String create() {

		return "/contents/create";
	}

	@PostMapping("/contents/create")
	public String create(ContentsDTO dto, String tag, HttpServletRequest request) throws IOException {// exception 지우기
		String upDir = new ClassPathResource("/static/images").getFile().getAbsolutePath();
		String fname = Utility.saveFileSpring(dto.getFilenameMF(), upDir);

		int size = (int) dto.getFilenameMF().getSize();

		if (size > 0) {
			dto.setFilename(fname);
		} else {
			dto.setFilename("default.jpg");
		}
		int cnt = service.create(dto);
		System.out.println("태그 값 확인:" + tag);
		if (tag.trim().length() != 0) {
			String t[] = tag.split("#");
			for (int i = 0; i < t.length; i++) {
				if (t[i].trim().length() != 0) {
					dto.setTag(t[i].trim().replace(" ", "_"));
					int cnt2 = service.create2(dto);
					int cnt3 = service.create3(dto);
					if (cnt3 <= 0) {
						return "error";
					}
				}
			}
		}else {
			if(cnt<=0) {
				return "error";
			}
		}
		return "redirect:/";
	}

	@GetMapping("/contents/update/{contentsno}")
	public String update(@PathVariable("contentsno") int contentsno, Model model) {
		ContentsDTO dto = service.detail(contentsno);
		model.addAttribute("contentsno", contentsno);
		System.out.println("파일이름:" + dto.getFilename());
		model.addAttribute("oldfile", dto.getFilename());
		model.addAttribute("dto", dto);
		if (dto.getTag() == null) {
			String tag = "";
		}
		return "/contents/update";
	}

	@PostMapping("/contents/update")
	public String update(ContentsDTO dto, String tag, int contentsno, MultipartFile filenameMF, String oldfile,
			HttpServletRequest request) throws IOException {
		String basePath = Contents.getUploadDir();
		if (oldfile != null && !oldfile.equals("default.jpg")) { // 원본파일 삭제
			Utility.deleteFile(basePath, oldfile);
		}
		// pstorage에 변경 파일 저장
		Map map = new HashMap();
		map.put("contentsno", contentsno);
		int size = (int) dto.getFilenameMF().getSize();
		if (size <= 0) {
			map.put("fname", oldfile);
		} else {
			map.put("fname", Utility.saveFileSpring(filenameMF, basePath));
		}
		// 디비에 파일명 변경
		int cnt = service.updateFile(map);
		int cnt2 = service.update(dto);
		int cnt5 = service.delete(contentsno);
		if (tag.trim().length() != 0) {
			String t[] = tag.split(",");
			for (int i = 0; i < t.length; i++) {
				if (t[i].trim().length() != 0) {
					dto.setTag(t[i].trim().replace(" ", "_"));
					int cnt3 = service.create2(dto);
					int cnt4 = service.update2(dto);
					if (cnt <= 0 || cnt2 <= 0 || cnt4 <= 0) {
						return "error";
					}
				}
			}
		}else {
			if(cnt<=0 || cnt2<=0 || cnt5<=0) {
				return "error";
			}
		}
		return "redirect:/";

	}

	@GetMapping("/contents/delete/{contentsno}")
	public String delete(@PathVariable("contentsno") int contentsno) {

		return "/contents/delete";
	}

	@PostMapping("/contents/delete")
	public String delete(HttpServletRequest request, int contentsno) {

		int cnt = service.delete(contentsno);
		int cnt2 = service.delete2(contentsno);
		if (cnt2 > 0) {
			return "redirect:/";
		} else {
			return "/error";
		}
	}

	@GetMapping("/contents/detail/{contentsno}")
	public String detail(@PathVariable("contentsno") int contentsno, Model model) {
		model.addAttribute("dto", service.detail(contentsno));
		
		return "/contents/detail";
	}

	@RequestMapping("/contents/list")
	public String list(HttpServletRequest request) {
		// 검색관련------------------------
		String col = Utility.checkNull(request.getParameter("col"));
		String word = Utility.checkNull(request.getParameter("word"));

		if (col.equals("total")) {
			word = "";
		}

		// 페이지관련-----------------------
		int nowPage = 1;// 현재 보고있는 페이지
		if (request.getParameter("nowPage") != null) {
			nowPage = Integer.parseInt(request.getParameter("nowPage"));
		}
		int recordPerPage = 5;// 한페이지당 보여줄 레코드갯수

		// DB에서 가져올 순번-----------------
		int sno = ((nowPage - 1) * recordPerPage) + 1;
		int eno = nowPage * recordPerPage;

		Map map = new HashMap();
		map.put("col", col);
		map.put("word", word);
		map.put("sno", sno);
		map.put("eno", eno);

		int total = service.total(map);

		List<ContentsDTO> list = service.list(map);

		String paging = Utility.paging(total, nowPage, recordPerPage, col, word);

		// request에 Model사용 결과 담는다
		request.setAttribute("list", list);
		request.setAttribute("nowPage", nowPage);
		request.setAttribute("col", col);
		request.setAttribute("word", word);
		request.setAttribute("paging", paging);
		
		return "/contents/list";

	}

	@GetMapping("/search")
	public String search() {
		return "/search";
	}

	@GetMapping(value = "/searchInput", produces = "application/json")
	@ResponseBody
	public List<Map> searchInput(HttpServletRequest request, HttpSession session) throws IOException {
		String id = (String) session.getAttribute("id");
		String searchInput = Utility.checkNull(request.getParameter("searchInput"));
		List<Map> searchlist = service.searchInput(searchInput);
		if (id == null) {
			searchlist = service.searchInput(searchInput);
		} else {
			searchlist = service.searchInput_privacy_not_zero(searchInput);
		}
		System.out.println(searchInput);
		System.out.println(searchlist);
		return searchlist;
	}

	@GetMapping("/search/friend")
	public String search_friend() {
		return "/search/friend";
	}

	@GetMapping(value = "/searchInput_friend", produces = "application/json")
	@ResponseBody
	public List<Map> searchInput_friend(HttpServletRequest request) throws IOException {
		String searchInput = Utility.checkNull(request.getParameter("searchInput_friend"));
		List<Map> searchFriendlist = service.searchInput_friend(searchInput);
		System.out.println(searchInput);
		System.out.println(searchFriendlist);
		return searchFriendlist;
	}

	@GetMapping(value = "/like", produces = "application/json")
	@ResponseBody
	public int like(HttpServletRequest request, HttpSession session) throws IOException {
		String id = (String) session.getAttribute("id");
		int contentsno = Integer.parseInt(request.getParameter("contentsno"));
		Map map = new HashMap();
		int cnt = 0;
		map.put("contentsno", contentsno);
		map.put("id", id);
		if (service.like(map) > 0) {
			service.updateLike(map);
			cnt = service.likeCnt(map);
		}
		return cnt;
	}

	@GetMapping(value = "/unlike", produces = "application/json")
	@ResponseBody
	public int unlike(HttpServletRequest request, HttpSession session) throws IOException {
		String id = (String) session.getAttribute("id");
		int contentsno = Integer.parseInt(request.getParameter("contentsno"));
		Map map = new HashMap();
		int cnt = 0;
		map.put("contentsno", contentsno);
		map.put("id", id);
		if (service.unlike(map) > 0) {
			service.updateLike(map);
			cnt = service.likeCnt(map);
		}
		return cnt;
	}
	// 댓글 작성
	@ResponseBody
	@GetMapping(value = "/write_reply")
	public List<ReplyDTO> write_reply(HttpServletRequest request, HttpSession session) {
		List<ReplyDTO> replyList = new ArrayList();
		ReplyDTO rto = new ReplyDTO();
		if(session.getAttribute("id") != null) {
			Map map = new HashMap();
			int contentsno = Integer.parseInt((String) request.getParameter("contentsno"));
			map.put("contentsno", contentsno);
			map.put("content", (String)request.getParameter("content"));
			map.put("id", (String) session.getAttribute("id"));
			if(replyService.write_reply(map)>0) {
				replyList = replyService.call_replyList(map);
				int reply = replyService.reply_count(map);
				map.put("reply", reply);
				replyService.replycnt_update(map);
			}
		}

	    return replyList;
	}

	// 댓글 삭제
	@ResponseBody
	@GetMapping(value = "/delete_reply")
	public int delete_reply(HttpServletRequest request, HttpSession session) {
		ReplyDTO rto = new ReplyDTO();
		Map map = new HashMap();
		int rnum = Integer.parseInt(request.getParameter("rnum"));
		System.out.println(rnum);
		int flag = 0;
		if(session.getAttribute("id") != null)	{
			map.put("id", (String)session.getAttribute("id"));
			map.put("rnum", rnum);
			rto = replyService.get_reply(rnum);
			flag = replyService.delete_reply(map);
			if(flag>0) {
				map.put("contentsno", rto.getContentsno());
				int reply = replyService.reply_count(map);
				map.put("reply", reply);
				replyService.replycnt_update(map);
				System.out.println("삭제 성공");
			}
		}
				
	    return flag;
	}
	
	// 댓글 리스트
		@ResponseBody
		@GetMapping(value = "/call_replyList", produces = "application/json")
		public List<ReplyDTO> reply_list(HttpServletRequest request, HttpSession session) {
			List<ReplyDTO> replyList = new ArrayList();
			Map map = new HashMap();
			System.out.println(request.getParameter("contentsno"));
			int contentsno = Integer.parseInt((String)request.getParameter("contentsno"));
			
			map.put("contentsno", contentsno);

		    replyList = replyService.call_replyList(map);
			
		    return replyList;
		}
}

