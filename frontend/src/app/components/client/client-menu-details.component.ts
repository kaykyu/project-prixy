import { Component, ElementRef, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Menu, MenuCategory } from '../../models';
import { ImageCroppedEvent, base64ToFile } from 'ngx-image-cropper';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-client-menu-details',
  templateUrl: './client-menu-details.component.html',
  styleUrl: './client-menu-details.component.css'
})
export class ClientMenuDetailsComponent implements OnInit, OnDestroy {

  private menu: Menu = inject(MAT_DIALOG_DATA)
  private clientSvc: ClientService = inject(ClientService)
  private fb: FormBuilder = inject(FormBuilder)
  private dialogRef: MatDialogRef<ClientMenuDetailsComponent> = inject(MatDialogRef)
  private dialog: MatDialog = inject(MatDialog)

  @ViewChild('cropper') cropper!: TemplateRef<any>
  @ViewChild('image') imageFile!: ElementRef
  dialog$!: Subscription
  form!: FormGroup
  selectedFile: any = null
  add: boolean = true
  categories: string[] = []
  imageChangedEvent: any = '';
  croppedImage: any = '';

  ngOnInit(): void {
    const keys = Object.keys(MenuCategory)
    this.categories = keys.slice(keys.length / 2)

    this.form = this.fb.group({
      name: this.fb.control<string>('', Validators.required),
      description: this.fb.control<string>(''),
      price: this.fb.control<number>(0, [Validators.required, Validators.min(0.01), Validators.max(999.99)]),
      category: this.fb.control<MenuCategory>(MenuCategory.OTHERS, Validators.required)
    })

    if (!!this.menu)
      this.add = false
    this.form.patchValue(this.menu)
  }

  ngOnDestroy(): void {
    if (!!this.dialog$)
      this.dialog$.unsubscribe()
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0] ?? null
    this.imageChangedEvent = event
    this.dialog$ = this.dialog.open(this.cropper).afterClosed().subscribe({
      next: value => {
        if (!value)
          this.clearImage()
      }
    })
  }

  process() {
    const formData = new FormData
    formData.set("name", this.form.value.name)
    formData.set("description", this.form.value.description)
    formData.set("price", this.form.value.price)
    formData.set("image", this.croppedImage)
    formData.set("category", this.form.value.category)

    if (!this.add) {
      formData.set("id", this.menu.id)
      this.clientSvc.putMenu(formData)
        .then(() => this.success())
        .catch(() => alert('Something went wrong.'))
    } else
      this.clientSvc.postMenu(formData)
        .then(() => this.success())
        .catch(() => alert('Something went wrong.'))
  }

  imageCropped(event: ImageCroppedEvent) {
    const file = this.imageFile.nativeElement.files[0]
    this.croppedImage = new File([base64ToFile(event.base64 as string)], file.name, { lastModified: file.lastModified, type: file.type })
  }

  clearImage() {
    this.imageFile.nativeElement.value = ''
    this.selectedFile = null
    this.croppedImage = null
  }

  success() {
    this.dialogRef.close(true)
    alert(`Menu successfully ${this.add ? 'added' : 'edited'}`)
  }
}