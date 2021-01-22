package com.claim.controller;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.claim.entity.SignIn;
import com.claim.entity.StringChecker;
import com.claim.entity.Student;
import com.claim.repository.StudentRepository;

// ctrl + shift + o is the shortcut to import or remove unused imports.

@CrossOrigin // this is only used at Claim. Security issue. We will have Tomcat and jsNode
				// servers creating separate domains.
@RestController
public class StudentController {

	@Autowired
	StudentRepository studRepo;

	@RequestMapping(value = "/submitStudentDetails", // This string is just a URL name, we can call it anything.
			consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, // Not required if we are
																										// returning void.
			method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> submitStudentDetails(@RequestBody Student currentStudent) {
		StringChecker check = new StringChecker();
		if (check.isEmail(currentStudent.getEmail())) {
			// Check for duplicate email addresses since this is supposed to be a unique ID value
			Optional<Student> duplicateEmail = this.studRepo.findByEmail(currentStudent.getEmail());
			if (duplicateEmail.isEmpty()) {
				this.studRepo.save(currentStudent); // This inserts currentStudent in the database.
				return new ResponseEntity<>("Student added successfully", HttpStatus.OK);
			}
			HttpHeaders header = new HttpHeaders();
			header.add("Email is already used", "Please enter another email or log in to your account.");
			return new ResponseEntity<>(header, HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>("Please enter a valid e-mail address", HttpStatus.LENGTH_REQUIRED);
	}

	@RequestMapping(value = "/findStudentById", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Optional<Student>> findStudent(String email) {
		if (email != null) {
			Optional<Student> foundStudent = this.studRepo.findByEmail(email);
			if (foundStudent.isPresent()) {
				return new ResponseEntity<>(foundStudent, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = "/loginStudent", // This string is just a URL name, we can call it anything.
			consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody // Can only handle one @RequestBody object at a time.
	public ResponseEntity<Optional<Student>> loginStudent(@RequestBody SignIn attempt) {
		if (!attempt.getUserID().isEmpty() && !attempt.getUserPassword().isEmpty()) {
			Optional<Student> foundStudent = this.studRepo.findByEmail(attempt.getUserID());
			if (foundStudent.isPresent()) {
				if (foundStudent.get().isCorrectPassword(attempt.getUserPassword())) {
					// For security return student with a blank password.
					foundStudent.get().setPassword("");
					return new ResponseEntity<>(foundStudent, HttpStatus.OK);
				}
			}
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	@RequestMapping(value = "/getStudentList", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ArrayList<Student>> getStudentList() {
		ArrayList<Student> studentList = new ArrayList<>(studRepo.findAll());
		return new ResponseEntity<>(studentList, HttpStatus.OK);
	}
}
