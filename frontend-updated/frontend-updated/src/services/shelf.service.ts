import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { UserShelfItem } from "../types/library";

export const getShelfAPI = (userId: number) =>
  api.get<UserShelfItem[]>(API_ENDPOINTS.SHELF.BASE(userId));
