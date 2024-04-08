import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { Menu } from '../../models';
import { ClientService } from '../../service/client.service';
import { MatDialog } from '@angular/material/dialog';
import { ClientMenuDetailsComponent } from './client-menu-details.component';

@Component({
  selector: 'app-client-menu',
  templateUrl: './client-menu.component.html',
  styleUrl: './client-menu.component.css'
})
export class ClientMenuComponent implements OnInit, OnDestroy {

  private clientSvc: ClientService = inject(ClientService)
  private dialog: MatDialog = inject(MatDialog)

  menu$!: Observable<Menu[]>
  dialog$!: Subscription
  categories: string[] = []

  ngOnInit(): void {
    this.clientSvc.getMenuCategory()
      .then(value => this.categories = value)

    this.menu$ = this.clientSvc.getMenu()
  }

  ngOnDestroy(): void {
    if (!!this.dialog$)
      this.dialog$.unsubscribe()
  }

  openAddDialog(item: Menu | void) {
    const dialogRef = this.dialog.open(ClientMenuDetailsComponent, { data: item });
    this.dialog$ = dialogRef.afterClosed().subscribe(
      result => {
        if (result) {
          this.menu$ = this.clientSvc.getMenu()
          this.clientSvc.getMenuCategory()
            .then(value => this.categories = value)
        }
      })
  }

  hasImage(menu: Menu): boolean {
      return menu.image !== 'https://vttp-kq.s3.ap-southeast-1.amazonaws.com/project/menu-placeholder.png'
  }

  removeImage(id: string, name: string) {
    if (confirm(`Confirm removal of image for ${name}?`))
      this.clientSvc.deleteMenuImage(id)
        .then(() => {
          this.menu$ = this.clientSvc.getMenu()
          this.clientSvc.getMenuCategory()
            .then(value => this.categories = value)
          this.clientSvc.openSnackBar(`${name} image successfully removed`)
        })
  }

  delete(id: string, name: string) {
    if (confirm(`Confirm deletion of ${name} from the menu?`))
      this.clientSvc.deleteMenu(id)
        .then(() => {
          this.menu$ = this.clientSvc.getMenu()
          this.clientSvc.getMenuCategory()
            .then(value => this.categories = value)
          this.clientSvc.openSnackBar(`${name} successfully deleted`)
        })
        .catch(() => alert(`Please make sure you have no pending orders for ${name} before deleting.`))
  }

  filter(menu: Menu[], category: string): Menu[] {
    return menu.filter(menu => menu.category.toString() == category)
  }

  search(menu: Menu[], search: string | undefined): Menu[] {
    if (!!search)
      return menu.filter(menu => menu.name.toLowerCase().includes(search.toLowerCase()))
    return menu
  }
}
