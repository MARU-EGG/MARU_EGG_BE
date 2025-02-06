package mju.iphak.maru_egg.answer.api.admin;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mju.iphak.maru_egg.answer.api.swagger.AdminAnswerControllerDocs;
import mju.iphak.maru_egg.answer.application.command.update.UpdateAnswerContent;
import mju.iphak.maru_egg.answer.api.dto.request.UpdateAnswerContentRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/answers")
public class AdminAnswerController implements AdminAnswerControllerDocs {

	private final UpdateAnswerContent updateAnswerContent;

	@PutMapping()
	public void updateAnswerContent(@Valid @RequestBody UpdateAnswerContentRequest request) {
		updateAnswerContent.invoke(request.id(), request.content());
	}
}
