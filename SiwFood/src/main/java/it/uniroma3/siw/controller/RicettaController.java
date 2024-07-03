package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.model.Ricetta;
import it.uniroma3.siw.repository.CuocoRepository;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.repository.IngredienteRepository;
import it.uniroma3.siw.repository.RicettaRepository;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.CuocoService;

@Controller
public class RicettaController {

	
	@Autowired 
	private RicettaRepository ricettaRepository;
	
	@Autowired 
	private CuocoRepository cuocoRepository;
	
	@Autowired 
	private CuocoService cuocoService;
	
	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private IngredienteRepository ingredienteRepository;
	
	@Autowired
	private CredentialsService credentialsService;
	
	@GetMapping("/ricette")
	public String getRicette(Model model) {		
		model.addAttribute("ricette", this.ricettaRepository.findAll());
		return "ricette.html";
	}
	
	@GetMapping("/ricetteCuoco")
	public String getRicetteCuoco(Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Cuoco currentUser = cuocoService.getUserByCredentials(userDetails).orElseThrow(() -> new RuntimeException("User not found"));
		List<Ricetta> ricette= currentUser.getRicette();
		model.addAttribute("ricette", ricette);
		return "ricetteCuoco.html";
	}
	
	@GetMapping("/ricetta/{id}")
	public String getRicetta(@PathVariable("id") Long id, Model model) {
		Ricetta ricetta= this.ricettaRepository.findById(id).get();
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("cuoco", ricetta.getCuoco());
		return "ricetta.html";
	}
	
	@GetMapping("/addRicetta")
	public String formNewRicetta(Model model) {
		model.addAttribute("ricetta", new Ricetta());
		return "admin/addRicetta.html";
	}
	
	@PostMapping("/adminAddRicetta")
    public String addRicetta(@ModelAttribute("ricetta") Ricetta ricetta, 
    		                    Model model,
    		                    @RequestParam("file") MultipartFile image) throws IOException {
        
		Image img = new Image(image.getBytes());
        this.imageRepository.save(img);
        ricetta.setImage(img);
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if(credentials.getRole().equals(Credentials.DEFAULT_ROLE)){
			Cuoco currentUser = cuocoService.getUserByCredentials(userDetails).orElseThrow(() -> new RuntimeException("User not found"));
			ricetta.setCuoco(currentUser);
		}
        this.ricettaRepository.save(ricetta);

		
		if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			return "redirect:/admin/indexRicette";  // Redirect o nome della vista dopo il salvataggio
		}
		
	
		return "redirect:/ricette";

    }
	
	@GetMapping("/modificaricetta/{id}")
	public String formAggiornaRicetta(@PathVariable("id") Long id, Model model) {
		Ricetta ricetta = ricettaRepository.findById(id).get();
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredienti", ricetta.getIngredienti());

		return "admin/formAggiornaRicetta.html";
	}

	@GetMapping(value="/admin/indexRicette")
	public String indexRicetta(Model model) {
		model.addAttribute("ricette", this.ricettaRepository.findAll());
		return "admin/indexRicette.html";
	}
	
	
	@GetMapping("/cancellaricetta/{ricettaId}")
	public String cancellaRicetta(Model model, @PathVariable("ricettaId") Long ricettaId ) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		this.ricettaRepository.delete(ricetta);
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			return "redirect:/admin/indexRicette";  // Redirect o nome della vista dopo il salvataggio
		}
		
	
		return "redirect:/ricetteCuoco";
	}
	
	@GetMapping(value="/admin/addCuocoRicetta/{id}")
	public String addCuocoRicettta(@PathVariable("id") Long id, Model model) {
		model.addAttribute("cuochi", cuocoRepository.findAll());
		model.addAttribute("ricetta", ricettaRepository.findById(id).get());
		return "admin/cuochiToAdd.html";
	}
	
	@GetMapping(value="/admin/setCuocoToRicetta/{cuocoId}/{ricettaId}")
	public String setCuocoToRicetta(@PathVariable("cuocoId") Long cuocoId, @PathVariable("ricettaId") Long ricettaId, Model model) {
		
		Cuoco cuoco = this.cuocoRepository.findById(cuocoId).get();
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		ricetta.setCuoco(cuoco);
		this.ricettaRepository.save(ricetta);
		
		model.addAttribute("ricetta", ricetta);
		return "admin/formAggiornaRicetta.html";
	}
	
	@PostMapping("/admin/addIngrediente/{ricettaId}")
	public String addIngredienteToRicetta(@PathVariable("ricettaId") Long ricettaId, @RequestParam String name, @RequestParam String quantita, Model model) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		Ingrediente ingrediente = new Ingrediente();
		ingrediente.setName(name);
		ingrediente.setQuantita(quantita);
		ricetta.getIngredienti().add(ingrediente);
		this.ingredienteRepository.save(ingrediente);
		this.ricettaRepository.save(ricetta);
		model.addAttribute("ricetta", ricetta);
		model.addAttribute("ingredienti", ricetta.getIngredienti());
		return "admin/formAggiornaRicetta.html";
	}

	@GetMapping("/admin/removeIngrediente/{ricettaId}/{ingredienteId}")
	public String removeIngredienteFromRicetta(@PathVariable("ricettaId") Long ricettaId, @PathVariable("ingredienteId") Long ingredienteId, Model model) {
		Ricetta ricetta = this.ricettaRepository.findById(ricettaId).get();
		Ingrediente ingrediente = this.ingredienteRepository.findById(ingredienteId).get();
        ricetta.getIngredienti().remove(ingrediente);
		this.ricettaRepository.save(ricetta);
		model.addAttribute("ricetta", ricetta);
		//model.addAttribute("ingredienti", ricetta.getIngredienti());
		return "redirect:/admin/formAggiornaRicetta";
	}


	@GetMapping(value="/admin/indexAdmin")
	public String indexAdmin(Model model) {
		return "admin/indexAdmin.html";
	}

	
}
