import { Injectable } from '@nestjs/common';
import { GPSLocation, Medicine, Pharmacy } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';
import { CreateMedicineDto } from './dtos/create-Medicine.dto';
import { getDistance } from 'geolib';

@Injectable()
export class MedicineService {
  constructor(private prismaService: PrismaService) {}

  calculateDistance(location1: GPSLocation, location2: GPSLocation): number {
    const distance = getDistance(
      { latitude: location1.lat, longitude: location1.lng },
      { latitude: location2.lat, longitude: location2.lng },
    );
    // The distance will be in meters
    return distance;
  }

  async getMedicines(
    query: string = '',
    pageNumber: number = 1,
  ): Promise<Partial<Medicine>[] | null> {
    const skip = (pageNumber - 1) * 10;
    const medicine = await this.prismaService.medicine.findMany({
      where: {
        OR: [{ name: { contains: query } }, { purpose: { contains: query } }],
      },
      select: {
        id: true,
        name: true,
        purpose: true,
        picture: true,
      },
      skip: skip,
      take: 10,
    });

    return medicine ?? null;
  }

  async getClosestPharmacies(
    medicineId: string,
    latitude: number,
    longitude: number,
  ): Promise<Pharmacy[]> {
    //closest pharmacies
    const avaliablePharmacies = await this.prismaService.stock.findMany({
      where: {
        medicineId: medicineId,
      },
      include: {
        pharmacy: true,
      },
    });

    // Calculate distances and sort pharmacies by distance
    const pharmaciesWithDistances = avaliablePharmacies.map((stock) => {
      const pharmacy = stock.pharmacy;
      const distance = this.calculateDistance(
        { lat: latitude, lng: longitude },
        {
          lat: pharmacy.location.lat,
          lng: pharmacy.location.lng,
        },
      );
      return { pharmacy, distance };
    });

    pharmaciesWithDistances.sort((a, b) => a.distance - b.distance);

    // Return the 10 closest pharmacies
    return pharmaciesWithDistances.slice(0, 10).map((entry) => entry.pharmacy);
  }

  async createMedicine(dto: CreateMedicineDto): Promise<Medicine> {
    return await this.prismaService.medicine.create({
      data: {
        name: dto.name,
        purpose: dto.purpose,
        picture: '',
      },
    });
  }
}
