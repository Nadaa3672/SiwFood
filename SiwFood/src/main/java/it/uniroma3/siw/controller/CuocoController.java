package it.uniroma3.siw.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.repository.CredentialsRepository;
import it.uniroma3.siw.repository.CuocoRepository;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.service.CredentialsService;

@Controller
public class CuocoController {
	
	@Autowired
	private CuocoRepository cuocoRepository;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private CredentialsRepository credentialsRepository;
	
	@Autowired
	private CredentialsService credentialsService;
	
	@GetMapping("/cuochi")
	public String getCuochi(Model model) {		
		model.addAttribute("cuochi", this.credentialsRepository.findNonAdminCuochi());
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
		model.addAttribute("credentials", new Credentials());
		return "admin/addCuoco.html";
	}
	
	@PostMapping("/adminAddCuoco")
    public String addCuoco(@ModelAttribute("cuoco") Cuoco cuoco, @ModelAttribute("credentials") Credentials credentials,
    		                    Model model,
    		                    @RequestParam("file") MultipartFile image) throws IOException {
        
		Image img = new Image(image.getBytes());
        this.imageRepository.save(img);
        cuoco.setImage(img);
        this.cuocoRepository.save(cuoco);
        
        credentials.setUser(cuoco);
        credentialsService.saveCredentials(credentials);
		
        return "redirect:/admin/indexCuochi"; // Redirect o nome della vista dopo il salvataggio
    }
	


	@GetMapping(value="/admin/indexCuochi")
	public String indexCuoco(Model model) {
		model.addAttribute("cuochi", this.credentialsRepository.findNonAdminCuochi());
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
		Credentials c= this.credentialsRepository.findByUser(cuoco);
		this.credentialsRepository.delete(c);
		this.cuocoRepository.delete(cuoco);
        

		return "redirect:/admin/indexCuochi";
	}
	
	
	@GetMapping("/modificacuoco/{id}")
	public String formAggiornaRicetta(@PathVariable("id") Long id, Model model) {		
		Cuoco cuoco = cuocoRepository.findById(id).get();
		model.addAttribute("cuoco", cuoco);


		return "admin/formAggiornaCuoco.html";
	}
	
	
	@PostMapping("/cambioNomeCuoco")
    public String cambioNomeCuoco(@RequestParam("id") Long id, @RequestParam("name") String name,Model model) {
        
		Cuoco cuoco=this.cuocoRepository.findById(id).get();
        cuoco.setName(name);
        this.cuocoRepository.save(cuoco);
        model.addAttribute("cuoco", cuoco);
        
        
	    return "admin/formAggiornaCuoco.html";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/cambioCognomeCuoco")
    public String cambioCognomeCuoco(@RequestParam("id") Long id, @RequestParam("surname") String surname,Model model) {
        
		Cuoco cuoco=this.cuocoRepository.findById(id).get();
        cuoco.setSurname(surname);
        this.cuocoRepository.save(cuoco);
        model.addAttribute("cuoco", cuoco);
        
	    return "admin/formAggiornaCuoco.html";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/cambioDataCuoco" )
    public String cambioDataCuoco(@RequestParam("id") Long id, @RequestParam("dateOfBirth") LocalDate data ,Model model) {
        
		Cuoco cuoco=this.cuocoRepository.findById(id).get();
        cuoco.setDateOfBirth(data);
        this.cuocoRepository.save(cuoco);
        model.addAttribute("cuoco", cuoco);
        
	    return "admin/formAggiornaCuoco.html";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/cambioEmailCuoco")
    public String cambioEmailCuoco(@RequestParam("id") Long id, @RequestParam("email") String email,Model model) {
        
		Cuoco cuoco=this.cuocoRepository.findById(id).get();
        cuoco.setEmail(email);
        this.cuocoRepository.save(cuoco);
        model.addAttribute("cuoco", cuoco);
        
	    return "admin/formAggiornaCuoco.html";  // Redirect o nome della vista dopo il salvataggio
    }
	
	@PostMapping("/immagineUpdateCuoco")
    public String cambioImmRicetta(@RequestParam("id") Long id, @RequestParam("file") MultipartFile image,Model model)throws IOException {
        
		Cuoco cuoco=this.cuocoRepository.findById(id).get();
		
		Image img = new Image(image.getBytes());
        this.imageRepository.save(img);
        cuoco.setImage(img);
        this.cuocoRepository.save(cuoco);
        model.addAttribute("cuoco", cuoco);

		
	    return "admin/formAggiornaCuoco.html";  // Redirect o nome della vista dopo il salvataggio
    }
	
}
