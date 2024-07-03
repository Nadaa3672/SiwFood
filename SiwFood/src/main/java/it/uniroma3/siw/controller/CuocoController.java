package it.uniroma3.siw.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.repository.CuocoRepository;
import it.uniroma3.siw.repository.ImageRepository;

@Controller
public class CuocoController {
	
	@Autowired 
	private CuocoRepository cuocoRepository;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@GetMapping("/cuochi")
	public String getCuochi(Model model) {		
		model.addAttribute("cuochi", this.cuocoRepository.findAll());
		return "cuochi.html";
	}
	
	@GetMapping("/cuoco/{id}")
	public String getCuoco(@PathVariable("id") Long id, Model model) {
		model.addAttribute("cuoco", this.cuocoRepository.findById(id).get());
		return "cuoco.html";
	}
	
	@GetMapping("/addCuoco")
	public String formNewCuoco(Model model) {
		model.addAttribute("cuoco", new Cuoco());
		return "admin/addCuoco.html";
	}
	
	@PostMapping("/adminAddCuoco")
    public String addCuoco(@ModelAttribute("cuoco") Cuoco cuoco, 
    		                    Model model,
    		                    @RequestParam("file") MultipartFile image) throws IOException {
        
		Image img = new Image(image.getBytes());
        this.imageRepository.save(img);
        cuoco.setImage(img);
        this.cuocoRepository.save(cuoco);
		
        return "redirect:/admin/indexCuochi"; // Redirect o nome della vista dopo il salvataggio
    }
	


	@GetMapping(value="/admin/indexCuochi")
	public String indexCuoco(Model model) {
		model.addAttribute("cuochi", this.cuocoRepository.findAll());
		return "admin/indexCuochi.html";
	}
	
/*	@PostMapping("/admin/cuoco")
	public String newArtist(@ModelAttribute("cuoco") Cuoco cuoco, Model model) {
		if (!cuocoRepository.existsByNameAndSurname(cuoco.getName(), cuoco.getSurname())) {
			this.cuocoRepository.save(cuoco); 
			model.addAttribute("cuoco", cuoco);
			return "cuoco.html";
		} else {
			model.addAttribute("messaggioErrore", "Questo cuoco esiste già");
			return "admin/formNewCuoco.html"; 
		}
	} */
	
	@GetMapping("/cancellacuoco/{cuocoId}")
	public String cancellaCuoco(Model model, @PathVariable("cuocoId") Long cuocoId ) {
		Cuoco cuoco = this.cuocoRepository.findById(cuocoId).get();
		this.cuocoRepository.delete(cuoco);
        

		return "/carrello";
	}
}
