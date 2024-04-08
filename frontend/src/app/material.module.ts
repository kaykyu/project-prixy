import { NgModule } from '@angular/core';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatStepperModule } from '@angular/material/stepper';
import { MatMenuModule } from '@angular/material/menu';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';

const MATERIAL = [
  MatGridListModule,
  MatIconModule,
  MatButtonModule,
  MatInputModule,
  MatFormFieldModule,
  MatToolbarModule,
  MatCardModule,
  MatDialogModule,
  MatDividerModule,
  MatSelectModule,
  MatTabsModule,
  MatTableModule,
  MatProgressBarModule,
  MatTooltipModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatStepperModule,
  MatMenuModule,
  MatBottomSheetModule,
  MatListModule,
  MatBadgeModule
]

@NgModule({
  declarations: [],
  imports: MATERIAL,
  exports: MATERIAL
})
export class MaterialModule { }
