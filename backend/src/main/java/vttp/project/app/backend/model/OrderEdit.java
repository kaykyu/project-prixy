package vttp.project.app.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEdit {
    
    private String id;
    private String item;
    private Integer progress;
    private Integer old;
    private Integer quantity;
}
