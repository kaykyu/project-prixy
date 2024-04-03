package vttp.project.app.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    private String id;

    @NotEmpty(message = "Name is required")
    private String name;

    private String description;
    private String image;

    @NotEmpty(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be more than 0.01")
    @DecimalMax(value = "999.99", message = "Price must be less than 999.99")
    private Double price;
    
    private MenuCategory category;
    
    public Menu(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public Menu(String name, String description, Double price, MenuCategory category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("name", this.name)
                .add("description", this.description != null ? this.description : "")
                .add("image",
                        this.image != null ? this.image
                                : "https://vttp-kq.s3.ap-southeast-1.amazonaws.com/project/menu-placeholder.png")
                .add("price", this.price)
                .add("category", this.category.toString())
                .build();
    }
}