import { CanActivateFn, Router } from "@angular/router";
import { inject } from "@angular/core";
import { User } from "./models";

export const isTokenValid: CanActivateFn =
    async (route, _state) => {
        const router = inject(Router)
        const token: string = route.params['token']
        const user: User = JSON.parse(atob(token))
        if (user.exp < Date.now() / 1000)
            return router.parseUrl('/error?error=expired');
        else if (user.iss !== 'Prixy')
            return router.parseUrl('/error?error=invalidlink')
        return true;
    }