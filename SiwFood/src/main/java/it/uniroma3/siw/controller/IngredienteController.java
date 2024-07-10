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

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Ingrediente;
import it.uniroma3.siw.repository.IngredienteRepository;
import it.uniroma3.siw.service.CredentialsService;

@Controller
public class IngredienteController {

	@Autowired
	private IngredienteRepository ingredienteRepository;
	
	@Autowired
	private CredentialsService credentialsService;
	
	@GetMapping("/addIngredienteCuoco")
	public String formNewIngrediente1(Model model) {
		model.addAttribute("ingrediente", new Ingrediente());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());
		return "addIngrediente.html";
	}
	
	@PostMapping("/cuocoAddIngrediente")
    public String cuocoAddIngrediente(@ModelAttribute("ingrediente") Ingrediente ingrediente, 
    		                    Model model) throws IOException {
        

        this.ingredienteRepository.save(ingrediente);
        
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if(credentials.getRole().equals(Credentials.ADMIN_ROLE)){
			return "redirect:/admin/indexRicette";
		}
        
        
        return "redirect:/ricette";  // Redirect o nome della vista dopo il salvataggio

    }
	
	@GetMapping("/removeIngrediente/{ingId}")
	public String cancellaIngrediente(Model model, @PathVariable("ingId") Long ingredienteId ) {
		
		Ingrediente ingrediente= this.ingredienteRepository.findById(ingredienteId).get();
        this.ingredienteRepository.delete(ingrediente);
        
        model.addAttribute("ingrediente", new Ingrediente());
		model.addAttribute("ingredienti", this.ingredienteRepository.findAll());

		return "addIngrediente";
	}
}
