package com.project.member;

<<<<<<< HEAD
public class MemberService {
=======
import java.util.List;
import java.util.Map;

public interface MemberService {

	int loginCheck(Map<String, String> map);

	MemberDTO read(String id);
	
	int duplicatedId(String id);

	int duplicatedEmail(String email);

	int create(MemberDTO dto);
>>>>>>> e638b8a2bf4c08cbb46d6772d85a407325657a51

}
